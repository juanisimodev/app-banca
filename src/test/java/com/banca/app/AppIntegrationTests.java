package com.banca.app;

import com.banca.app.domain.Account;
import com.banca.app.domain.AccountType;
import com.banca.app.domain.Role;
import com.banca.app.domain.User;
import com.banca.app.repository.AccountRepository;
import com.banca.app.repository.MovementRepository;
import com.banca.app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Tests de integracion de flujos web principales:
 * autenticacion, transferencias y administracion de usuarios.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AppIntegrationTests {

    private static final String DEST_RUT = "22.222.222-2";
        private static final String MANAGER_RUT = "2-7";

    @Autowired
        private WebApplicationContext webApplicationContext;

        private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private MovementRepository movementRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

        /**
         * Inicializa MockMvc y datos minimos requeridos para escenarios de prueba.
         */
    @BeforeEach
    void setUpDestinationUser() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        if (userRepository.findByRut(DEST_RUT).isPresent()) {
            return;
        }

        User user = new User();
        user.setRut(DEST_RUT);
        user.setNombreCompleto("Cliente Destino Test");
        user.setPassword(passwordEncoder.encode("dest123"));
        user.setRole(Role.USER);
        User saved = userRepository.save(user);

        Account ahorro = new Account();
        ahorro.setNumeroCuenta(DEST_RUT + "-A");
        ahorro.setTipo(AccountType.AHORRO);
        ahorro.setSaldo(new BigDecimal("20000.00"));
        ahorro.setUsuario(saved);

        Account corriente = new Account();
        corriente.setNumeroCuenta(DEST_RUT + "-C");
        corriente.setTipo(AccountType.CORRIENTE);
        corriente.setSaldo(new BigDecimal("10000.00"));
        corriente.setUsuario(saved);

        accountRepository.save(ahorro);
        accountRepository.save(corriente);

        if (userRepository.findByRut(MANAGER_RUT).isEmpty()) {
            User manager = new User();
            manager.setRut(MANAGER_RUT);
            manager.setNombreCompleto("Gerente Integracion");
            manager.setPassword(passwordEncoder.encode("manager123"));
            manager.setRole(Role.GERENTE);
            userRepository.save(manager);
        }
    }

    /**
     * Verifica que la vista de login este disponible y renderice contenido base.
     */
    @Test
    void loginPageShouldLoad() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(content().string(containsString("App Banca")));
    }

    /**
     * Verifica la visualizacion de mensaje de error cuando login falla.
     */
    @Test
    void loginPageWithErrorParamShouldRenderErrorMessage() throws Exception {
        mockMvc.perform(get("/login").param("error", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("error"))
                .andExpect(content().string(containsString("incorrectos")));
    }

    /**
     * Comprueba que dashboard requiere autenticacion.
     */
    @Test
    void dashboardShouldRequireAuthentication() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    /**
     * Valida el dashboard para usuario final.
     */
    @Test
    @WithMockUser(username = "12.345.678-9", roles = "USER")
    void userDashboardShouldRenderUserData() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attribute("isUser", is(true)))
                .andExpect(model().attributeExists("usuario", "cuentas", "movimientos"))
                .andExpect(content().string(containsString("Bienvenido")));
    }

    /**
     * Valida el dashboard para gerente con sus indicadores y tablas.
     */
    @Test
    @WithMockUser(username = MANAGER_RUT, roles = "GERENTE")
    void managerDashboardShouldRenderMonitoringData() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attribute("isManager", is(true)))
                .andExpect(model().attributeExists(
                        "managerTotalClientes",
                        "managerTotalSaldos",
                        "managerTransferenciasHoy",
                        "managerAlertasHoy",
                        "managerClients",
                        "managerRecentMovements"))
                .andExpect(content().string(containsString("Monitoreo de movimientos")));
    }

    /**
     * Valida el dashboard para administrador del sistema.
     */
    @Test
    @WithMockUser(username = "1-9", roles = "SYS_ADMIN")
    void adminDashboardShouldRenderAdminData() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attribute("isAdmin", is(true)))
                .andExpect(model().attributeExists("createUserRequest", "adminRoles", "adminUsers"))
                .andExpect(content().string(containsString("Administracion: Crear Usuario")));
    }

    @Test
    @WithMockUser(username = "12.345.678-9", roles = "USER")
        /**
         * Verifica transferencia exitosa con impacto en saldos y trazabilidad.
         */
    void userTransferShouldUpdateBalancesAndCreateMovements() throws Exception {
        Account origen = accountRepository.findByUsuarioRutAndTipo("12.345.678-9", AccountType.AHORRO)
                .orElseThrow();
        Account destino = accountRepository.findByUsuarioRutAndTipo(DEST_RUT, AccountType.CORRIENTE)
                .orElseThrow();

        BigDecimal origenInicial = origen.getSaldo();
        BigDecimal destinoInicial = destino.getSaldo();
        long movimientosIniciales = movementRepository.count();

        mockMvc.perform(post("/transfer")
                        .with(csrf())
                        .param("tipoCuentaOrigen", "AHORRO")
                        .param("rutDestino", DEST_RUT)
                        .param("tipoCuentaDestino", "CORRIENTE")
                        .param("monto", "5000.00")
                        .param("descripcion", "Transferencia test"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(flash().attributeExists("success"));

        Account origenActualizada = accountRepository.findByUsuarioRutAndTipo("12.345.678-9", AccountType.AHORRO)
                .orElseThrow();
        Account destinoActualizada = accountRepository.findByUsuarioRutAndTipo(DEST_RUT, AccountType.CORRIENTE)
                .orElseThrow();

        assertThat(origenActualizada.getSaldo()).isEqualByComparingTo(origenInicial.subtract(new BigDecimal("5000.00")));
        assertThat(destinoActualizada.getSaldo()).isEqualByComparingTo(destinoInicial.add(new BigDecimal("5000.00")));
        assertThat(movementRepository.count()).isEqualTo(movimientosIniciales + 2);
    }

    @Test
    @WithMockUser(username = "12.345.678-9", roles = "USER")
        /**
         * Verifica validacion de monto no permitido en formulario de transferencia.
         */
    void transferWithInvalidAmountShouldReturnTransferView() throws Exception {
        long movimientosIniciales = movementRepository.count();

        mockMvc.perform(post("/transfer")
                        .with(csrf())
                        .param("tipoCuentaOrigen", "AHORRO")
                        .param("rutDestino", DEST_RUT)
                        .param("tipoCuentaDestino", "CORRIENTE")
                        .param("monto", "0"))
                .andExpect(status().isOk())
                .andExpect(view().name("transfer"))
                .andExpect(model().attributeExists("error", "usuario", "cuentas", "accountTypes"));

        assertThat(movementRepository.count()).isEqualTo(movimientosIniciales);
    }

    @Test
    @WithMockUser(username = "12.345.678-9", roles = "USER")
        /**
         * Verifica error de negocio por fondos insuficientes sin efectos secundarios.
         */
    void transferWithInsufficientFundsShouldRedirectToTransferWithoutCreatingMovements() throws Exception {
        Account origen = accountRepository.findByUsuarioRutAndTipo("12.345.678-9", AccountType.AHORRO)
                .orElseThrow();
        BigDecimal saldoInicial = origen.getSaldo();
        long movimientosIniciales = movementRepository.count();

        mockMvc.perform(post("/transfer")
                        .with(csrf())
                        .param("tipoCuentaOrigen", "AHORRO")
                        .param("rutDestino", DEST_RUT)
                        .param("tipoCuentaDestino", "CORRIENTE")
                        .param("monto", "999999999.00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/transfer"))
                .andExpect(flash().attributeExists("error"));

        Account origenActualizada = accountRepository.findByUsuarioRutAndTipo("12.345.678-9", AccountType.AHORRO)
                .orElseThrow();

        assertThat(origenActualizada.getSaldo()).isEqualByComparingTo(saldoInicial);
        assertThat(movementRepository.count()).isEqualTo(movimientosIniciales);
    }

    @Test
    @WithMockUser(username = "12.345.678-9", roles = "USER")
        /**
         * Verifica error de negocio cuando la cuenta destino no existe.
         */
    void transferToNonExistingDestinationShouldRedirectToTransferWithoutCreatingMovements() throws Exception {
        Account origen = accountRepository.findByUsuarioRutAndTipo("12.345.678-9", AccountType.AHORRO)
                .orElseThrow();
        BigDecimal saldoInicial = origen.getSaldo();
        long movimientosIniciales = movementRepository.count();

        mockMvc.perform(post("/transfer")
                        .with(csrf())
                        .param("tipoCuentaOrigen", "AHORRO")
                        .param("rutDestino", "99.999.999-9")
                        .param("tipoCuentaDestino", "CORRIENTE")
                        .param("monto", "5000.00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/transfer"))
                .andExpect(flash().attributeExists("error"));

        Account origenActualizada = accountRepository.findByUsuarioRutAndTipo("12.345.678-9", AccountType.AHORRO)
                .orElseThrow();

        assertThat(origenActualizada.getSaldo()).isEqualByComparingTo(saldoInicial);
        assertThat(movementRepository.count()).isEqualTo(movimientosIniciales);
    }

    @Test
    @WithMockUser(username = "1-9", roles = "SYS_ADMIN")
        /**
         * Verifica que un administrador pueda crear usuarios con cuentas base.
         */
    void adminShouldCreateUserWithDefaultAccounts() throws Exception {
        String newRut = "33.333.333-3";

        mockMvc.perform(post("/admin/users")
                        .with(csrf())
                        .param("rut", newRut)
                        .param("nombreCompleto", "Usuario Integracion")
                        .param("password", "pass123")
                        .param("role", "USER"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(flash().attributeExists("success"));

        User creado = userRepository.findByRut(newRut).orElse(null);
        assertThat(creado).isNotNull();

        Account ahorro = accountRepository.findByUsuarioRutAndTipo(newRut, AccountType.AHORRO).orElse(null);
        Account corriente = accountRepository.findByUsuarioRutAndTipo(newRut, AccountType.CORRIENTE).orElse(null);

        assertThat(ahorro).isNotNull();
        assertThat(corriente).isNotNull();
        assertThat(ahorro.getSaldo()).isEqualByComparingTo("50000.00");
        assertThat(corriente.getSaldo()).isEqualByComparingTo("100000.00");
    }

    @Test
    @WithMockUser(username = "12.345.678-9", roles = "USER")
        /**
         * Verifica que un usuario no admin no pueda crear usuarios nuevos.
         */
    void nonAdminShouldNotCreateUsers() throws Exception {
        String blockedRut = "44.444.444-4";

        mockMvc.perform(post("/admin/users")
                        .with(csrf())
                        .param("rut", blockedRut)
                        .param("nombreCompleto", "No Permitido")
                        .param("password", "pass123")
                        .param("role", "USER"))
                .andExpect(status().isForbidden());

        assertThat(userRepository.findByRut(blockedRut)).isEmpty();
    }
}
