package com.bolsaideas.datajpa.controllers;

import java.io.IOException;
import java.net.MalformedURLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.bolsaideas.datajpa.models.entity.Cliente;
import com.bolsaideas.datajpa.models.service.IClienteService;
import com.bolsaideas.datajpa.models.service.IUploadFileService;
import com.bolsaideas.datajpa.util.paginator.PageRender;

import jakarta.validation.Valid;

@Controller
@SessionAttributes("cliente")
public class ClienteController {

	@Autowired
	private IClienteService clienteService;

	@Autowired
	private IUploadFileService uploadService;

	@GetMapping("/uploads/{filename:.+}")
	public ResponseEntity<Resource> verFoto(@PathVariable(value = "filename") String filename) {
		Resource recurso = null;
		try {
			recurso = uploadService.load(filename);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + recurso.getFilename() + "\"")
				.body(recurso);
	}

	@GetMapping("/ver/{id}")
	public String ver(@PathVariable(value = "id") Long id, Model model, RedirectAttributes redirectAttributes) {
		Cliente cliente = clienteService.findOne(id);
		if (id > 0) {
			if (cliente == null) {
				redirectAttributes.addFlashAttribute("danger",
						"Error: hubo un error al intentar ver el detalle del cliente, el cliente no existe en el sistema");
				return "redirect:/listar";
			}
		} else {
			redirectAttributes.addFlashAttribute("danger",
					"Error: hubo un error al intentar ver el detalle del cliente, la ID no puede ser 0");
			return "redirect:/listar";
		}
		model.addAttribute("titulo", "Detalle Cliente: " + cliente.getNombre());
		model.addAttribute("cliente", cliente);
		return "ver";
	}

	@GetMapping("/listar")
	public String listar(@RequestParam(name = "page", defaultValue = "0") int page, Model model) {
		Pageable pageRequest = PageRequest.of(page, 4);
		Page<Cliente> clientes = clienteService.findAll(pageRequest);
		PageRender<Cliente> pageRender = new PageRender<>("/listar", clientes);
		model.addAttribute("titulo", "Listado de clientes");
		model.addAttribute("clientes", clientes);
		model.addAttribute("page", pageRender);
		return "listar";
	}

	@GetMapping("/form")
	public String crear(Model model) {
		Cliente cliente = new Cliente();
		model.addAttribute("titulo", "Formulario de cliente");
		model.addAttribute("cliente", cliente);
		return "form";
	}

	@SuppressWarnings("unused")
	@PostMapping("/form")
	public String guardar(@Valid Cliente cliente, BindingResult bindingResult, Model model,
			@RequestParam("file") MultipartFile foto, RedirectAttributes redirectAttributes,
			SessionStatus sessionStatus) {
		if (bindingResult.hasErrors()) {
			model.addAttribute("titulo", "Formulario de Cliente");
			return "form";
		}

		if (!foto.isEmpty()) {
			if (cliente.getId() != null && cliente.getId() > 0 && cliente.getFoto() != null
					&& cliente.getFoto().length() > 0) {
				uploadService.delete(cliente.getFoto());
			}
			String uniqueFilename = null;
			try {
				uniqueFilename = uploadService.copy(foto);
			} catch (IOException e) {
				e.printStackTrace();
			}
			redirectAttributes.addFlashAttribute("info", "Has subido correctamente '" + uniqueFilename + "'");
			cliente.setFoto(uniqueFilename);
		}

		String mensajeFlash = (cliente.getId() != null) ? "Cliente editado con exito" : "Cliente creado con exito";

		clienteService.save(cliente);
		sessionStatus.setComplete();
		redirectAttributes.addFlashAttribute("success", mensajeFlash);
		return "redirect:listar";
	}

	@GetMapping("/form/{id}")
	public String editar(@PathVariable(value = "id") Long id, Model model, RedirectAttributes redirectAttributes) {
		Cliente cliente = null;

		if (id > 0) {
			cliente = clienteService.findOne(id);
			if (cliente == null) {
				redirectAttributes.addFlashAttribute("danger",
						"Error: hubo un error al editar el cliente, el cliente no existe en el sistema");
				return "redirect:/listar";
			}
		} else {
			redirectAttributes.addFlashAttribute("danger",
					"Error: hubo un error al editar el cliente, la ID no puede ser 0");
			return "redirect:/listar";
		}
		model.addAttribute("titulo", "Editar Cliente");
		model.addAttribute("cliente", cliente);
		return "form";
	}

	@GetMapping("/eliminar/{id}")
	public String eliminar(@PathVariable(value = "id") Long id, RedirectAttributes redirectAttributes) {
		if (id > 0) {
			Cliente cliente = clienteService.findOne(id);
			clienteService.delete(id);
			redirectAttributes.addFlashAttribute("success", "Cliente eliminado con exito");
			if(cliente.getFoto() != null) {
				if (uploadService.delete(cliente.getFoto())) {
					redirectAttributes.addFlashAttribute("info", "Foto eliminada con exito: " + cliente.getFoto());
				}
			}
		}
		return "redirect:/listar";
	}
}
