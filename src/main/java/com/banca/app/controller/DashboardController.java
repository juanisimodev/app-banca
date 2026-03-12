package com.banca.app.controller;

import com.banca.app.domain.AccountType;
import com.banca.app.domain.Movement;
import com.banca.app.domain.Role;
import com.banca.app.domain.User;
import com.banca.app.dto.AdminUserSummaryDTO;
import com.banca.app.dto.CreateUserDTO;
import com.banca.app.dto.ManagerClientSummaryDTO;
import com.banca.app.dto.ManagerMovementSummaryDTO;
import com.banca.app.repository.AccountRepository;
import com.banca.app.repository.MovementRepository;
import com.banca.app.repository.UserRepository;
import com.banca.app.service.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * Controlador principal del dashboard para USER, GERENTE y SYS_ADMIN.
 */
@Controller
@RequiredArgsConstructor
@RequestMapping
public class DashboardController {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final MovementRepository movementRepository;
    private final AdminUserService adminUserService;

    private static final BigDecimal MOVIMIENTO_ALTO_UMBRAL = new BigDecimal("500000");

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Obtener el usuario autenticado desde el contexto de seguridad
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String rut = authentication.getName(); // El username es el RUT
        
        // Buscar el usuario en la base de datos
        User usuario = userRepository.findByRut(rut)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        // Agregar datos al modelo para la vista
        model.addAttribute("usuario", usuario);
        model.addAttribute("cuentas", List.of());
        model.addAttribute("movimientos", List.of());

        boolean isAdmin = usuario.getRole() == Role.SYS_ADMIN;
        boolean isManager = usuario.getRole() == Role.GERENTE;
        boolean isUser = usuario.getRole() == Role.USER;

        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("isManager", isManager);
        model.addAttribute("isUser", isUser);

        if (isUser) {
            List<Long> cuentaIds = usuario.getCuentas().stream()
                .map(cuenta -> cuenta.getId())
                .collect(Collectors.toList());

            List<Movement> movimientos = cuentaIds.isEmpty()
                ? List.of()
                : movementRepository.findByCuentaIdInOrderByFechaDesc(cuentaIds)
                .stream()
                .limit(5)
                .collect(Collectors.toList());

            model.addAttribute("cuentas", usuario.getCuentas());
            model.addAttribute("movimientos", movimientos);
        }

        if (isAdmin) {
            if (!model.containsAttribute("createUserRequest")) {
                CreateUserDTO createUserDTO = new CreateUserDTO();
                createUserDTO.setRole(Role.USER);
                model.addAttribute("createUserRequest", createUserDTO);
            }
            model.addAttribute("adminRoles", new Role[]{Role.USER, Role.GERENTE, Role.SYS_ADMIN});

            List<AdminUserSummaryDTO> adminUsers = userRepository.findAll().stream()
                    .sorted(Comparator.comparing(User::getNombreCompleto, String.CASE_INSENSITIVE_ORDER))
                    .map(user -> {
                        BigDecimal saldoAhorro = user.getCuentas().stream()
                                .filter(cuenta -> cuenta.getTipo() == AccountType.AHORRO)
                                .map(cuenta -> cuenta.getSaldo() == null ? BigDecimal.ZERO : cuenta.getSaldo())
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                        BigDecimal saldoCorriente = user.getCuentas().stream()
                                .filter(cuenta -> cuenta.getTipo() == AccountType.CORRIENTE)
                                .map(cuenta -> cuenta.getSaldo() == null ? BigDecimal.ZERO : cuenta.getSaldo())
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                        BigDecimal saldoTotal = user.getCuentas().stream()
                                .map(cuenta -> cuenta.getSaldo() == null ? BigDecimal.ZERO : cuenta.getSaldo())
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                        return new AdminUserSummaryDTO(
                                user.getRut(),
                                user.getNombreCompleto(),
                                user.getRole(),
                                saldoAhorro,
                                saldoCorriente,
                                saldoTotal
                        );
                    })
                    .collect(Collectors.toList());

            model.addAttribute("adminUsers", adminUsers);
        }

    if (isManager) {
        LocalDate hoy = LocalDate.now();

        List<User> clientes = userRepository.findAll().stream()
            .filter(user -> user.getRole() == Role.USER)
            .collect(Collectors.toList());

        BigDecimal totalSaldosBanco = accountRepository.findAll().stream()
            .map(cuenta -> cuenta.getSaldo() == null ? BigDecimal.ZERO : cuenta.getSaldo())
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Movement> movimientosGlobales = movementRepository.findAll().stream()
            .sorted(Comparator.comparing(Movement::getFecha).reversed())
            .collect(Collectors.toList());

        long transferenciasHoy = movimientosGlobales.stream()
            .filter(m -> m.getFecha() != null && m.getFecha().toLocalDate().equals(hoy))
            .filter(m -> m.getDescripcion() != null && m.getDescripcion().startsWith("Transferencia"))
            .count();

        long alertasHoy = movimientosGlobales.stream()
            .filter(m -> m.getFecha() != null && m.getFecha().toLocalDate().equals(hoy))
            .filter(m -> m.getMonto() != null && m.getMonto().abs().compareTo(MOVIMIENTO_ALTO_UMBRAL) >= 0)
            .count();

        List<ManagerClientSummaryDTO> managerClients = clientes.stream()
            .sorted(Comparator.comparing(User::getNombreCompleto, String.CASE_INSENSITIVE_ORDER))
            .map(user -> {
            BigDecimal saldoAhorro = user.getCuentas().stream()
                .filter(cuenta -> cuenta.getTipo() == AccountType.AHORRO)
                .map(cuenta -> cuenta.getSaldo() == null ? BigDecimal.ZERO : cuenta.getSaldo())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal saldoCorriente = user.getCuentas().stream()
                .filter(cuenta -> cuenta.getTipo() == AccountType.CORRIENTE)
                .map(cuenta -> cuenta.getSaldo() == null ? BigDecimal.ZERO : cuenta.getSaldo())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal saldoTotal = user.getCuentas().stream()
                .map(cuenta -> cuenta.getSaldo() == null ? BigDecimal.ZERO : cuenta.getSaldo())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            LocalDateTime ultimoMovimiento = user.getCuentas().stream()
                .flatMap(cuenta -> cuenta.getMovimientos().stream())
                .map(Movement::getFecha)
                .filter(fecha -> fecha != null)
                .max(LocalDateTime::compareTo)
                .orElse(null);

            return new ManagerClientSummaryDTO(
                user.getRut(),
                user.getNombreCompleto(),
                saldoAhorro,
                saldoCorriente,
                saldoTotal,
                ultimoMovimiento
            );
            })
            .collect(Collectors.toList());

        List<ManagerMovementSummaryDTO> managerRecentMovements = movimientosGlobales.stream()
            .limit(30)
            .map(m -> new ManagerMovementSummaryDTO(
                m.getFecha(),
                m.getCuenta().getUsuario().getRut(),
                m.getCuenta().getUsuario().getNombreCompleto(),
                m.getCuenta().getNumeroCuenta(),
                m.getDescripcion(),
                m.getMonto(),
                m.getMonto() != null && m.getMonto().abs().compareTo(MOVIMIENTO_ALTO_UMBRAL) >= 0
            ))
            .collect(Collectors.toList());

        model.addAttribute("managerTotalClientes", clientes.size());
        model.addAttribute("managerTotalSaldos", totalSaldosBanco);
        model.addAttribute("managerTransferenciasHoy", transferenciasHoy);
        model.addAttribute("managerAlertasHoy", alertasHoy);
        model.addAttribute("managerClients", managerClients);
        model.addAttribute("managerRecentMovements", managerRecentMovements);
        model.addAttribute("managerHighAmountThreshold", MOVIMIENTO_ALTO_UMBRAL);
    }
        
        return "dashboard";
    }

    @PostMapping("/admin/users")
    public String createUser(
            @Valid @ModelAttribute("createUserRequest") CreateUserDTO request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentRut = authentication.getName();
        User currentUser = userRepository.findByRut(currentRut)
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado"));

        if (currentUser.getRole() != Role.SYS_ADMIN) {
            redirectAttributes.addFlashAttribute("error", "No tienes permisos para crear usuarios.");
            return "redirect:/dashboard";
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Datos invalidos para crear usuario.");
            redirectAttributes.addFlashAttribute("createUserRequest", request);
            return "redirect:/dashboard";
        }

        try {
            adminUserService.createUserWithDefaultAccounts(
                    request.getRut(),
                    request.getNombreCompleto(),
                    request.getPassword(),
                    request.getRole());

            redirectAttributes.addFlashAttribute("success", "Usuario creado correctamente.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            redirectAttributes.addFlashAttribute("createUserRequest", request);
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "No se pudo crear el usuario.");
            redirectAttributes.addFlashAttribute("createUserRequest", request);
        }

        return "redirect:/dashboard";
    }
}
