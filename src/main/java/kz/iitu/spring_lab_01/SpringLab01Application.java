package kz.iitu.spring_lab_01;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
public class SpringLab01Application {

	public static void main(String[] args) {
		SpringApplication.run(SpringLab01Application.class, args);
	}
}