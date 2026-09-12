package com.joaogabrielbmf.gestao_vendas.config;
import com.joaogabrielbmf.gestao_vendas.repository.UsuarioRepository;
import org.springframework.context.annotation.*;import org.springframework.security.config.annotation.web.builders.HttpSecurity;import org.springframework.security.core.userdetails.*;import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;import org.springframework.security.crypto.password.PasswordEncoder;import org.springframework.security.web.SecurityFilterChain;
@Configuration public class SecurityConfig{
 @Bean PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder();}
 @Bean UserDetailsService uds(UsuarioRepository repo){return email->{var u=repo.findByEmailIgnoreCase(email).orElseThrow(()->new UsernameNotFoundException("Usuário não encontrado"));return User.withUsername(u.getEmail()).password(u.getSenhaHash()).roles("USER").build();};}
 @Bean SecurityFilterChain filter(HttpSecurity h)throws Exception{h.csrf(c->c.disable()).authorizeHttpRequests(a->a.requestMatchers("/login.html","/css/**","/js/login.js","/auth/cadastro").permitAll().anyRequest().authenticated()).formLogin(f->f.loginPage("/login.html").loginProcessingUrl("/login").defaultSuccessUrl("/",true).failureUrl("/login.html?erro=1").permitAll()).logout(l->l.logoutUrl("/logout").logoutSuccessUrl("/login.html"));return h.build();}
}
