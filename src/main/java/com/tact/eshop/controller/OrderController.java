package com.tact.eshop.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tact.eshop.Application;
import com.tact.eshop.entity.Customer;
import com.tact.eshop.entity.Order;
import com.tact.eshop.entity.OrderProduct;
import com.tact.eshop.entity.Product;
import com.tact.eshop.repository.CustomerRepository;
import com.tact.eshop.repository.OrderProductRepository;
import com.tact.eshop.repository.OrderRepository;
import com.tact.eshop.repository.ProductRepository;


@Controller
@RequestMapping("/order/")
public class OrderController {
	private static final Logger log =
            LoggerFactory.getLogger(Application.class);

	@Autowired
	private OrderRepository oRepo;
	
	@Autowired
	private CustomerRepository cRepo;
	
	@Autowired
	private ProductRepository pRepo;
	
	@Autowired
	private OrderProductRepository opRepo;
//list product	
	@RequestMapping("list")
	public String list(HttpSession session, Model model) {
		String result;
		//if the account  and different from null
		if(session.getAttribute("account") != null) {
			// the user is assigned to a session
			Customer client = (Customer) session.getAttribute("account");
			//on list the customer order
			List<Order> order = (List<Order>) oRepo.findAll();
			ArrayList<Order> ordersOfCustomer = new ArrayList<Order>();
			//we look at all the orders
			for(Order orderList : order) {
				//if one of the orders and equal to the customer order is added
				if(orderList.getCustomer().getId() == client.getId()) {
					
					ordersOfCustomer.add(orderList);
				}
			}
			//model recovers the attribut of the customer's order
			model.addAttribute("orders", ordersOfCustomer);
			result = "/order/list";
		}
		
		else {
			result = "redirect:/";
		}
		
		return result;
	}
//description	
	@RequestMapping("{id}")
	public String description(@PathVariable String id, HttpSession session, Model model) {
		
		String result;
		
		Order order = oRepo.findOne(Long.valueOf(id));
		//if the account  and different from null
		if(session.getAttribute("account") != null) {
			Customer currentClient = (Customer) session.getAttribute("account");
			//if current customer id and equal to customer order id 
			if(currentClient.getId() == order.getCustomer().getId()) {
			//show item description
				model.addAttribute("order", order);
				
				result = "order/description";
			}
			else {
				//otherwise show the list
				result = "redirect:/order/list";
			}
		}
		//return to reception
		else {
			result = "redirect:/";
		}
		
		return result;
	}
//add product to chart 	
	@RequestMapping("add/{id}")
	public String addProductToChart(@PathVariable String id, HttpSession session, Model model) {
		
		String result = "redirect:/product/{id}";
		//if the account  and different from null
		if(session.getAttribute("account") != null) {
			Customer currentClient = (Customer) session.getAttribute("account");
			currentClient = cRepo.findOne(currentClient.getId());
			//if the order in cour and equal to null
			if(session.getAttribute("currentOrder") == null) {
				
				List<Order> ordersClient = currentClient.getOrders();
				//if Customer order is equal to 0
				if(ordersClient.size() == 0) {
					// save the selected article
					Order order = new Order();
					currentClient.addOrder(order);
					oRepo.save(order);
					session.setAttribute("currentOrder", order);
				 
				}
				else {
					
					ArrayList<Order> allOrders = new ArrayList<Order>();
					
					for(Order orderList : ordersClient) {
						if(orderList.getFinished() == false) {
							allOrders.add(orderList);
						}
					}
					//if orders equal one 
					if(allOrders.size() == 1) {
						session.setAttribute("currentOrder", ordersClient.get(0));
					}
					else {
						session.setAttribute("account", null);
						model.addAttribute("error", "An error was occur, please connect again<br>If the problem, contact support system");
						result = "/user/connexion";
					}
				}
			}
			
			if(session.getAttribute("currentOrder") != null) {
				Product productToAdd = pRepo.findOne(Long.valueOf(id));
				
				if(productToAdd.getEndOfLife() == false) {					
					Order newOrder = (Order) session.getAttribute("currentOrder");
					
					Boolean check = false;
					OrderProduct newOrderProduct = new OrderProduct(newOrder, productToAdd, 1);
					
					for(OrderProduct orderProduct : newOrder.getOrderedProduct()) {
						if(orderProduct.getId() == productToAdd.getId()) {
							orderProduct.setQuantity(orderProduct.getQuantity() + 1);
							productToAdd.setQuantity(productToAdd.getQuantity() - 1);
							newOrder.setTotal(newOrder.getTotal() + productToAdd.getPrice());
							newOrderProduct = orderProduct;
							check = true;
							break;
						}
					}
					
					if(!check) {
						newOrder.addProduct(productToAdd, 1);
						newOrderProduct = newOrder.getOrderedProduct().get(newOrder.getOrderedProduct().size() - 1);
					}
					
					opRepo.save(newOrderProduct);
					oRepo.save(newOrder);
				}
			}
		}
		
		return result;
	}
//remove product to chart	
	@RequestMapping("remove/{id}")
	public String removeProductToChart(@PathVariable String id, HttpSession session, Model model) {
		String result = "redirect:/order/list";
		
		if(session.getAttribute("account") != null) {
			
			Customer currentClient = (Customer) session.getAttribute("account");
			currentClient = cRepo.findOne(currentClient.getId());
			if(session.getAttribute("currentOrder") == null) {
				
				List<Order> ordersClient = currentClient.getOrders();

				if(ordersClient.size() == 0) {
					Order order = new Order();
					currentClient.addOrder(order);
					oRepo.save(order);
					session.setAttribute("currentOrder", order);
				}
				else {
					
					ArrayList<Order> allOrders = new ArrayList<Order>();
					
					for(Order orderList : ordersClient) {
						if(orderList.getFinished() == false) {
							allOrders.add(orderList);
						}
					}
					
					if(allOrders.size() == 1) {
						session.setAttribute("currentOrder", ordersClient.get(0));
					}
					else {
						session.setAttribute("account", null);
						model.addAttribute("error", "An error was occur, please connect again<br>If the problem, contact support system");
						result = "/user/connexion";
					}
				}
			}
			
			if(session.getAttribute("currentOrder") != null) {
				Product productErase = pRepo.findOne(Long.valueOf(id));
								
				Order newOrder = (Order) session.getAttribute("currentOrder");
				
				
				for(OrderProduct orderProduct : newOrder.getOrderedProduct()) {
					if(orderProduct.getId() == productErase.getId()) {
						newOrder.removeProduct(productErase, 1);
						OrderProduct newOrderProduct = orderProduct;
						if(newOrderProduct.getQuantity() == 0) {
							opRepo.delete(newOrderProduct);
						}
						else {
							opRepo.save(newOrderProduct);
						}
						break;
					}
				}
												
				oRepo.save(newOrder);
				
			}
		}
		return result;
	}

}
