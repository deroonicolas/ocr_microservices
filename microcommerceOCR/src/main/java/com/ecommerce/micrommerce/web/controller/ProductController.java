package com.ecommerce.micrommerce.web.controller;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.ecommerce.micrommerce.web.dao.ProductDao;
import com.ecommerce.micrommerce.web.exception.ProduitGratuitException;
import com.ecommerce.micrommerce.web.exception.ProduitIntrouvableException;
import com.ecommerce.micrommerce.web.model.Product;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api("API pour les opérations CRUD sur les produits.")
@RestController
public class ProductController {

	private final ProductDao productDao;

	public ProductController(ProductDao productDao) {
		this.productDao = productDao;
	}

	@GetMapping("/Produits")
	public MappingJacksonValue listeProduits() {
		List<Product> produits = productDao.findAll();
		SimpleBeanPropertyFilter monFiltre = SimpleBeanPropertyFilter.serializeAllExcept("prixAchat");
		FilterProvider listDeNosFiltres = new SimpleFilterProvider().addFilter("monFiltreDynamique", monFiltre);
		MappingJacksonValue produitsFiltres = new MappingJacksonValue(produits);
		produitsFiltres.setFilters(listDeNosFiltres);
		return produitsFiltres;
	}

	@ApiOperation(value = "Récupère un produit grâce à son ID à condition que celui-ci soit en stock!")
	@GetMapping(value = "/Produits/{id}")
	public Optional<Product> afficherUnProduit(@PathVariable int id) {
	   Optional<Product> produit = productDao.findById(id);
	   if (produit.isEmpty()) {
		   throw new ProduitIntrouvableException("Le produit avec l'id " + id + " est INTROUVABLE. Écran Bleu si je pouvais.");
	   } else {
		   return produit;
	   }
	}

	@GetMapping(value = "/produits/pasChers/{prixLimit}")
	public List<Product> afficherProduitsPasCher(@PathVariable int prixLimit) {
		return productDao.findByPrixLessThan(prixLimit);
	}

	@PostMapping(value = "/Produits")
	public ResponseEntity<Product> ajouterProduit(@Valid @RequestBody Product product) {
		if (product.getPrix() == 0) {
			throw new ProduitGratuitException("Le produit ne peut être gratuit !");
		}
		Product productAdded = productDao.save(product);
		if (Objects.isNull(productAdded)) {
			return ResponseEntity.noContent().build();
		}
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
				.buildAndExpand(productAdded.getId()).toUri();
		return ResponseEntity.created(location).build();
	}

	@DeleteMapping(value = "/Produits/{id}")
	public void supprimerProduit(@PathVariable int id) {
		productDao.deleteById(id);
	}

	@PutMapping(value = "/Produits")
	public void updateProduit(@RequestBody Product product) {
		productDao.save(product);
	}
	
	// EXOS
	@GetMapping(value = "/AdminProduitsMarge")
	public Map<Product, Integer> calculerMargeProduit() {
		List<Product> products =  productDao.findAll();
		Map<Product, Integer> finalProducts =  new HashMap<Product, Integer>();
		for (Product p : products) {
			int marge = p.getPrix() - p.getPrixAchat();
			finalProducts.put(p, marge);
		}
		return finalProducts;
	}
	
	@GetMapping(value = "/OrdreAlpha")
	public List<Product> trierProduitsParOrdreAlphabetique() {
		return productDao.findAllByOrderByNomAsc();
	}
}