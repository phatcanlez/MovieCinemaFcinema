package com.example.projectwebmovie.mapper;

import com.example.projectwebmovie.dto.RegisterDTO;
import com.example.projectwebmovie.model.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AccountMapperImpl implements AccountMapper {

    private static final Logger logger = LoggerFactory.getLogger(AccountMapperImpl.class);

    @Override
    public Account toAccount(RegisterDTO registerDTO) {
        logger.info("Mapping RegisterDTO to Account for username: {}", registerDTO.getUsername());

        Account account = new Account();
        account.setUsername(registerDTO.getUsername());
        account.setPassword(registerDTO.getPassword()); // Sẽ được mã hóa sau
        account.setEmail(registerDTO.getEmail());
        // Các trường không bắt buộc được để null trong AccountService
        logger.debug("Mapped RegisterDTO to Account successfully for username: {}", registerDTO.getUsername());
        return account;
    }
}