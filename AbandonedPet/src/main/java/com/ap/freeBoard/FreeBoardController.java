package com.ap.freeBoard;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ap.member.Member;
import com.ap.member.MemberService;

@Controller
@RequestMapping("/freeBoard")
public class FreeBoardController {

	@Autowired
	FreeBoardService freeBoardService;
	
	@Autowired
	MemberService memberService;
	
	
	
	@GetMapping("/addFreeBoard")
	public String requestAddNoticeForm(FreeBoard freeBoard) {
		return "/freeBoard/addFreeBoard";
	}
	
	@PostMapping("/addFreeBoard")
	public String newPost(@Valid FreeBoard freeBoard, Errors errors) {
		if(errors.hasErrors()) {
			return "/freeBoard/addFreeBoard";
		}
		freeBoardService.insert(freeBoard);
		return "redirect:/freeBoard/freeBoardList";
	}
	
	

	
	@GetMapping("/freeBoardList")
	public String freeBoardList(Model m, String page, String keyword, String type) {
		int pageSize = 10;
		int intPage = 1;
		
		if(page != null) {
			intPage = Integer.parseInt(page);
		}
		
		
		Map<String, Object> pagination = new HashMap<String, Object>();
		pagination.put("pageSize", pageSize);
		pagination.put("pageNum", intPage);
		pagination.put("keyword", keyword);
		pagination.put("type", type);
		List<FreeBoard> freeBoardList = freeBoardService.getAllList(pagination);		
		int cnt = freeBoardService.countList(pagination);

		
		m.addAttribute("freeBoardList",freeBoardList);
		m.addAttribute("cnt", cnt);
		m.addAttribute("page", intPage);
		m.addAttribute("keyword", keyword);
		m.addAttribute("type", type);
		return "/freeBoard/freeBoardList";
	}
	
	@GetMapping("/freeBoardDetail")
	public String requestById(String bid, Model m, HttpServletRequest request, HttpServletResponse response) {
		
		SecurityContext securityContext = SecurityContextHolder.getContext();
		Authentication auth = securityContext.getAuthentication();
	    
	    String username = null;
	    
	    if(auth.getPrincipal() instanceof Member) {
			Member principal = (Member) auth.getPrincipal();
			username = principal.getUsername();
			m.addAttribute("member", memberService.getMember(username));
		} else {
			username = auth.getName();
			m.addAttribute("member", memberService.getMember(username));
		}
		
		// 조회수 증가
		Cookie oldCookie = null;
	    Cookie[] cookies = request.getCookies();
	    // postView쿠키를 찾아서 oldCookie에 넣는다.
	    if (cookies != null) {
	        for (Cookie cookie : cookies) {
	            if (cookie.getName().equals("postView")) {
	                oldCookie = cookie;
	            }
	        }
	    }
	    
	    // oldCookie에서 해당 세션ID의 값이 있는지 조회한다.
	    if(oldCookie == null) {
	    	// 조회수1 증가
	    	freeBoardService.updateHit(bid);
	    	Cookie newCookie = new Cookie("postView", "["+bid+"]");
	    	newCookie.setPath("/");
	    	newCookie.setMaxAge(60 * 60 * 24);
	        response.addCookie(newCookie);
	    } else {
	    	if(!oldCookie.getValue().contains("["+bid+"]")) {
	    		//조회수 1증가
	    		freeBoardService.updateHit(bid);
	    		oldCookie.setValue(oldCookie.getValue() + "_[" + bid + "]");
	    		oldCookie.setPath("/");
	    		oldCookie.setMaxAge(60*60*24);
	    		response.addCookie(oldCookie);
	    	}
	    }
		
		
		int bid2 = Integer.parseInt(bid);
		FreeBoard freeBoardById = freeBoardService.getById(bid2);
		m.addAttribute("freeBoard", freeBoardById);
		
//		System.out.println(member.getUsername());
//		System.out.println(freeBoardById.getBwriter());
//		System.out.println(member.getUsername().equals(freeBoardById.getBwriter()));
		
		
		return "/freeBoard/freeBoardDetail";
	}
	
	@PostMapping("/freeBoardDelete")
	public String delete(String bid) {
		int bid2 = Integer.parseInt(bid);
		freeBoardService.delete(bid2);
		return "/freeBoard/freeBoardList";
	}
	
	@GetMapping("/freeBoardModify")
	public String modify(String bid, Model m, FreeBoard freeBoard) {
		int bid2 = Integer.parseInt(bid);
		FreeBoard boardById = freeBoardService.getById(bid2);
		m.addAttribute("freeBoard", boardById);
		return "/freeBoard/freeBoardModify";
	}
	
	@PostMapping("/freeBoardModify")
	public String modifyBoard(@Valid FreeBoard freeBoard, Errors errors, Model m) {
		
		if(errors.hasErrors()) {
			m.addAttribute("bid", freeBoard.getBid());
			return "/freeBoard/freeBoardModify";
		}
		
		freeBoardService.update(freeBoard);
		
		return "redirect:/freeBoard/freeBoardDetail?bid=" + freeBoard.getBid();
	}
	
	@ResponseBody
	@PostMapping("/addReply")
	public String addReply(Reply reply) {
		freeBoardService.insertReply(reply);
		return "/freeBoard/freeBoardList";
	}
	
	@ResponseBody
	@PostMapping("/replyList")
	public List<Reply> replyList(String bid){
		int bid2 = Integer.parseInt(bid);
		return freeBoardService.replyList(bid2);
	}
	
	@ResponseBody
	@PostMapping("/modifyReply")
	public void modifyReply(Reply reply) {
		freeBoardService.replyUpdate(reply);
	}
	
	@ResponseBody
	@PostMapping("/deleteReply")
	public void deleteReply(String rid) {
		int rid2 = Integer.parseInt(rid);
		freeBoardService.replyDelete(rid2);
	}
	
	
}
