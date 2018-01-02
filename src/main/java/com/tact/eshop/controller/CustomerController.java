package com.tact.eshop.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tact.eshop.entity.Customer;
import com.tact.eshop.repository.CustomerRepository;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
@RequestMapping("/user/")
public class CustomerController {
	
	@Autowired
	private CustomerRepository cRepo;
	
	@RequestMapping("profil")
	public String profil() {
		
		return "/user/profil";
	}
	
	@RequestMapping("connexion")
	public String connexion(HttpSession session) {
		String returnString;
		if(session.getAttribute("account") != null) {
			returnString = "redirect:/";
		}
		else {
			returnString = "/user/connexion";
		}
		return returnString;
		
	}
	
	@RequestMapping("disconnexion")
	public String disconnexion(HttpSession session) {
		if(session.getAttribute("account") != null) {
			session.invalidate();
		}
		return "redirect:/";
	}
	
	//firstname and lastname verification
	@PostMapping("authentification")
	public String authentification(Customer customer, HttpSession session, Model model) {
		//create a customerList list that retrieves the lastname
		List<Customer> ListClient = cRepo.findByLastName(customer.getLastName());
		ArrayList<Customer> Focused = new ArrayList<Customer>();
		//retrieves the FirstName
		for(Customer clientInList : ListClient) {
			if(clientInList.getFirstName().equals(customer.getFirstName())) {
				Focused.add(clientInList);
			}
		}
		
		if(!Focused.isEmpty()) {
			//if lastName and firstname exists redirection to the home page
			session.setAttribute("account", Focused.get(0));
			return "redirect:/";
		}
		else{
			//identification error
			model.addAttribute("error", "Aucune personne de ce nom n'est inscrite sur notre site");
			return "/user/connexion";
		}
	}
	//retrieve the data related to the connection
	@PostMapping("modification")
	public String modification(Customer customer, HttpSession session) {
		Customer newClient = (Customer) session.getAttribute("account");
		
		newClient.setFirstName(customer.getFirstName());
		newClient.setLastName(customer.getLastName());
		newClient.setAddress(customer.getAddress());
		newClient.setCity(customer.getCity());
		newClient.setPhone(customer.getPhone());
		newClient.setZip(customer.getZip());
		
		cRepo.save(newClient);
		session.setAttribute("account", newClient);
		
		return "/user/profil";
	}

}
