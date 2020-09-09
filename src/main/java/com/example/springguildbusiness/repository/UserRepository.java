package com.example.springguildbusiness.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.springguildbusiness.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

}
