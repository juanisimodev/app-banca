package com.banca.app.controller;

import com.banca.app.domain.AccountType;
import com.banca.app.domain.User;
import com.banca.app.dto.TransferRequestDTO;
import com.banca.app.exception.AccountNotFoundException;
import com.banca.app.exception.InsufficientFundsException;
import com.banca.app.exception.TransferException;
import com.banca.app.repository.UserRepository;
import com.banca.app.service.TransactionService;
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

/**
 * Controla el flujo de transferencias entre cuentas de clientes.
 */
@Controller
@RequestMapping("/transfer")
@RequiredArgsConstructor
public class TransferController {

    private final TransactionService transactionService;
    private final UserRepository userRepository;

    /**
     * Muestra el formulario de transferencia
     */
    @GetMapping
    public String showTransferForm(Model model) {
        if (!model.containsAttribute("transferRequest")) {
            model.addAttribute("transferRequest", new TransferRequestDTO());
        }
        
        // Obtener usuario autenticado para mostrar sus cuentas
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String rut = authentication.getName();
        
        User usuario = userRepository.findByRut(rut)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        model.addAttribute("usuario", usuario);
        model.addAttribute("cuentas", usuario.getCuentas());
        model.addAttribute("accountTypes", AccountType.values());
        
        return "transfer";
    }

    /**
     * Procesa la transferencia
     */
    @PostMapping
    public String processTransfer(
            @Valid @ModelAttribute("transferRequest") TransferRequestDTO transferRequest,
            BindingResult bindingResult,
            @ModelAttribute("tipoCuentaOrigen") AccountType tipoCuentaOrigen,
            RedirectAttributes redirectAttributes,
            Model model) {
        
        // Validar errores de validación del DTO
        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "Por favor, corrige los errores en el formulario");
            
            // Recargar datos del usuario
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String rut = authentication.getName();
            User usuario = userRepository.findByRut(rut)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            model.addAttribute("usuario", usuario);
            model.addAttribute("cuentas", usuario.getCuentas());
            model.addAttribute("accountTypes", AccountType.values());
            
            return "transfer";
        }
        
        try {
            // Obtener el RUT del usuario autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String rutOrigen = authentication.getName();
            
            // Realizar la transferencia
            transactionService.realizarTransferencia(
                    rutOrigen,
                    tipoCuentaOrigen,
                    transferRequest.getRutDestino(),
                    transferRequest.getTipoCuentaDestino(),
                    transferRequest.getMonto()
            );
            
            // Transferencia exitosa
            redirectAttributes.addFlashAttribute("success", 
                    String.format("Transferencia exitosa de $%s a la cuenta %s-%s",
                            transferRequest.getMonto(),
                            transferRequest.getRutDestino(),
                            transferRequest.getTipoCuentaDestino()));
            
            return "redirect:/dashboard";
            
        } catch (InsufficientFundsException e) {
            // Saldo insuficiente
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("transferRequest", transferRequest);
            return "redirect:/transfer";
            
        } catch (AccountNotFoundException e) {
            // Cuenta no encontrada
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("transferRequest", transferRequest);
            return "redirect:/transfer";
            
        } catch (TransferException e) {
            // Error general de transferencia
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("transferRequest", transferRequest);
            return "redirect:/transfer";
            
        } catch (Exception e) {
            // Cualquier otro error inesperado
            redirectAttributes.addFlashAttribute("error", 
                    "Error inesperado al procesar la transferencia. Por favor, intente nuevamente.");
            redirectAttributes.addFlashAttribute("transferRequest", transferRequest);
            return "redirect:/transfer";
        }
    }
}
