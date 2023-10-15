package br.com.phenriquep00.todolist.filter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.phenriquep00.todolist.user.IUserRepository;
import br.com.phenriquep00.todolist.user.UserModel;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Base64;

@Component
public class FilterTaskAuth extends OncePerRequestFilter
{

    @Autowired
    private IUserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException
    {

        var servletPath = request.getServletPath();
        if(!servletPath.startsWith("/tasks/"))
        {
            filterChain.doFilter(request, response);
            return;
        } else
        {
            // get user credentials
            String[] credentials = this.getBasicAuthCredentials(request);
            if (credentials == null)
            {
                response.sendError(401, "Unauthorized (failed to parse credentials)");
                return;
            }

            String username = credentials[0];
            String password = credentials[1];

            // check if user exists
            UserModel user = this.userRepository.findByUsername(username);

            if (user == null)
            {
                response.sendError(401, "Unauthorized (invalid user)");
            } else
            {
                // validate password
                var passwordVerify = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());

                if (!passwordVerify.verified)
                {
                    response.sendError(401, "Unauthorized (invalid password)");
                    return;
                }

                request.setAttribute("idUser", user.getId());
                filterChain.doFilter(request, response);
            }
        }


    }

    private String[] getBasicAuthCredentials(HttpServletRequest request)
    {
        String authorization = request.getHeader("Authorization");

        if (authorization == null)
        {
            return null;
        }

        String[] parts = authorization.split(" ");

        if (parts.length != 2)
        {
            return null;
        }

        if (!parts[0].equalsIgnoreCase("Basic"))
        {
            return null;
        }

        return new String(Base64.getDecoder().decode(parts[1])).split(":");
    }

}
