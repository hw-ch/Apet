package com.ap.member;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
//ConstraintValidator 인터페이스를 구현하는 클래스
public class UniqueUsernameValidator implements ConstraintValidator<UniqueUsername, String> {

	 private final MemberRepository memberRepository;
	
	 @Override
	 public boolean isValid(String username, ConstraintValidatorContext context) {
	     // 데이터베이스에 접근하여 중복된 아이디가 있는지 검증
	     return memberRepository.countIdDuplicated(username) == 0;
	 }
}