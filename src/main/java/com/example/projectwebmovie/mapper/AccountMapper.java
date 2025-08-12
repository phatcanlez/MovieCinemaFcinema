package com.example.projectwebmovie.mapper;

import com.example.projectwebmovie.dto.RegisterDTO;
import com.example.projectwebmovie.model.Account;

public interface AccountMapper {
    Account toAccount(RegisterDTO registerDTO);
}