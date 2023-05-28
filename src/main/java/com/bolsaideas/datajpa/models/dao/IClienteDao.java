package com.bolsaideas.datajpa.models.dao;


import org.springframework.data.jpa.repository.JpaRepository;

import com.bolsaideas.datajpa.models.entity.Cliente;

public interface IClienteDao extends JpaRepository<Cliente, Long>{
	
}