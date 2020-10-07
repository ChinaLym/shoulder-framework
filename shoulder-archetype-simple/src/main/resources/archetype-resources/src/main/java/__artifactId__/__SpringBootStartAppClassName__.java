package ${package}.${artifactId};

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 启动类
 *
 * @author lym
 */
@SpringBootApplication
public class ${SpringBootStartAppClassName} {

	public static void main(String[] args) {
		SpringApplication.run(${SpringBootStartAppClassName}.class, args);
	}

}
