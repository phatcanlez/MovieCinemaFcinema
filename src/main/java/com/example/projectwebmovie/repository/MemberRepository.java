package com.example.projectwebmovie.repository;

import com.example.projectwebmovie.model.Account;
import com.example.projectwebmovie.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, String> {
    Member findByAccount(Account account);
}