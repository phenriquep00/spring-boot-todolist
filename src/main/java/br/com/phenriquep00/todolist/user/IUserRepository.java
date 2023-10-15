package br.com.phenriquep00.todolist.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IUserRepository extends JpaRepository<UserModel, UUID>
{
    UserModel findByUsername(String username);
}
