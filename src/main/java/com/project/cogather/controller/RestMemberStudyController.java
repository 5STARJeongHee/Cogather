package com.project.cogather.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.cogather.domain.AjaxResult;
import com.project.cogather.domain.MemberStudyDTO;
import com.project.cogather.domain.MemberStudyResult;
import com.project.cogather.domain.MembersDTO;
import com.project.cogather.service.MemberStudyService;

@RestController
@RequestMapping("/group/MemberStudyRest")
public class RestMemberStudyController {
	
	@Autowired
	MemberStudyService memberStudyService;
	
	// memberStudy 에서 studygroup 아이디에 따라 참여하고 있는 멤버데이터를 json으로 반환하는 핸드
	@GetMapping("/ms/{sg_id}")
	public MemberStudyResult getMemberStudy(@PathVariable int sg_id){
		MemberStudyResult result = new MemberStudyResult();
		List<MemberStudyDTO> ms = null;
		List<MembersDTO> m = null;
		ms = memberStudyService.select(sg_id);
		m = memberStudyService.selectMembersBySGId(sg_id);
		int cnt = 0; 
		String status = "fail"; 
		StringBuilder message = new StringBuilder(); 
		if(ms == null || ms.size() == 0 || m == null || m.size() == 0) {
			message.append("StudyGroup data 가져오기 실패");
		}else {
			status = "OK";
			cnt = ms.size();
		}
		
		result.setCnt(cnt);
		result.setMessage(message.toString());
		result.setStatus(status);
		result.setMSdata(ms);
		result.setMdata(m);
		return result;
	}
	
	
	@PutMapping("/ms/acctime")
	public AjaxResult updateAcctime(int sg_id, String id, String acctime) {
		AjaxResult result = new AjaxResult();
		int cnt = 0; 
		String status = "fail"; 
		StringBuilder message = new StringBuilder();
		
		List<MemberStudyDTO> ms = memberStudyService.getAcctime(sg_id, id);
		if(ms == null || ms.size() == 0) {
			message.append("Acctime data 가져오기 실패");
		}
		
		if(ms.get(0).getAcctime() == null) {
			
			cnt = memberStudyService.updateAcctime(sg_id, id, LocalDateTime.parse(acctime, DateTimeFormatter.ISO_DATE_TIME));
		}else {
			LocalDateTime temp = ms.get(0).getAcctime();
			LocalDateTime time = LocalDateTime.parse(acctime, DateTimeFormatter.ISO_DATE_TIME);
			temp.plusMonths(time.getMonthValue());
			temp.plusDays(time.getDayOfMonth());
			temp.plusHours(time.getHour());
			temp.plusMinutes(time.getMinute());
			
			cnt = memberStudyService.updateAcctime(sg_id, id, temp);
		}
		
		
		
		if(cnt == 0) {
			message.append("Acctime update 실패");
		}else {
			status = "OK";
		}
		result.setCnt(cnt);
		result.setMessage(message.toString());
		result.setStatus(status);
		return result;
	}
}
