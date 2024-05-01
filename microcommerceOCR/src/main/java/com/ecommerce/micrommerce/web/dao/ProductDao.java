package com.ecommerce.micrommerce.web.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecommerce.micrommerce.web.model.Product;

@Repository
public interface ProductDao extends JpaRepository<Product, Integer> {
	List<Product> findByPrixLessThan(int prixLimit);
	@Query("SELECT id, nom, prix FROM Product p WHERE p.prix < :prixLimit")
	List<Product> chercherUnProduitPasCher(@Param("prixLimit") int prix);
	List<Product> findAllByOrderByNomAsc();
}
