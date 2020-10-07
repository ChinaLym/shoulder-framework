package ${package}.${artifactId}.config;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import ${package}.${artifactId}.${SpringBootStartAppClassName};

/**
 * 打成 war 包部署到外部tomcat需要这个
 *
 * @author lym
 */
//@Configuration
public class ServletInitializer extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(${SpringBootStartAppClassName}.class);
	}

}
