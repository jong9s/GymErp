package com.example.gymerp;


import java.util.TimeZone;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

import jakarta.annotation.PostConstruct;



//resources 에 작성한 custom.properties 파일을 로딩하도록 한다.


@PropertySource(value="classpath:custom.properties")
@SpringBootApplication
//Mapper 인터페이스를 만들지 않고 sqlSession 으로 처리할 때 패키지 인식이 안되는 경우가 생김
//@MapperScan 으로 mapper 를 읽을 수 있도록 설정
@MapperScan(basePackages = "com.example.gymerp.repository")
public class GymErpApplication {

	public static void main(String[] args) {
		
		SpringApplication.run(GymErpApplication.class, args);
	}

	@PostConstruct                                
    public void setTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    }
}
