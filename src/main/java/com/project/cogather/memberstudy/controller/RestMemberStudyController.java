package com.project.cogather.memberstudy.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.cogather.common.AjaxResult;
import com.project.cogather.members.model.MembersDTO;
import com.project.cogather.memberstudy.model.MemberStudyDTO;
import com.project.cogather.memberstudy.model.MemberStudyResult;
import com.project.cogather.memberstudy.model.MemberStudyUserResult;
import com.project.cogather.memberstudy.service.MemberStudyService;
import com.project.cogather.studygroup.model.StudyGroupDTO;
import com.project.cogather.studygroup.service.StudyGroupService;

@RestController
@RequestMapping("/group/MemberStudyRest")
public class RestMemberStudyController {

	@Autowired
	MemberStudyService memberStudyService;

	@Autowired
	StudyGroupService studyGroupService;
	
	@GetMapping("/{sg_id}")
	public MemberStudyUserResult getUserList(
			@NotNull @Positive @PathVariable Integer sg_id) {
		MemberStudyUserResult result = new MemberStudyUserResult();
		List<MemberStudyDTO> rdata = null;
		List<MembersDTO> rmember = null;
		List<MemberStudyDTO> cdata = null;
		List<MembersDTO> cmember = null;
		
		int cnt = 0;
		String status = "fail";
		StringBuilder message = new StringBuilder();
		
		try {
			System.out.println("check0");
			rdata = memberStudyService.selectRegister(sg_id);
			System.out.println("check1");
			rmember = memberStudyService.selectRegisterMember(sg_id);
			System.out.println("check2");
			cdata = memberStudyService.selectCommon(sg_id);
			System.out.println("check3");
			cmember = memberStudyService.selectCommonMember(sg_id);
			System.out.println("check4");
			
			if (rdata == null || rdata.size() == 0 || rmember == null || rmember.size() == 0) {
				message.append("생성자는 어디 있냐");
				
			}else {
				status = "OK";
			}
			
			if (cdata == null || cdata.size() == 0 || cmember == null || cmember.size() == 0) {
				message.append("참가 신청한 인원이 없음");
			}
			
		}catch(Exception e) {
			message.append("트랜잭션 에러: " + e.getMessage());
		}
		
		result.setMessage(message.toString());
		result.setStatus(status);
		result.setRdata(rdata);
		result.setRmember(rmember);
		result.setCdata(cdata);
		result.setCmember(cmember);
		
		return result;
	}
	

	// memberStudy 에서 studygroup 아이디에 따라 참여하고 있는 멤버데이터를 json으로 반환하는 핸들러
	@GetMapping("/ms/{sg_id}")
	public MemberStudyResult getMemberStudy(
			@NotNull @Positive @PathVariable Integer sg_id) {
		MemberStudyResult result = new MemberStudyResult();
		List<MemberStudyDTO> ms = null;
		List<MembersDTO> m = null;
		List<StudyGroupDTO> study = null;
		int cnt = 0;
		String status = "fail";
		StringBuilder message = new StringBuilder();
		
		try {
			ms = memberStudyService.select(sg_id);
			m = memberStudyService.selectMembersBySGId(sg_id);
			study = studyGroupService.getStudyBySgID(sg_id);
			if (ms == null || ms.size() == 0 || m == null || m.size() == 0 || study == null || study.size() ==0 ) {
				message.append("StudyGroup data 가져오기 실패");
			} else {
				status = "OK";
				cnt = ms.size();
			}

		}catch(Exception e) {
			message.append(e.getMessage());
		}
		System.out.println("들어온 멤버수: "+ cnt);
		result.setCnt(cnt);
		result.setMessage(message.toString());
		result.setStatus(status);
		result.setMSdata(ms);
		result.setMdata(m);
		result.setStudy(study);
		return result;
	}

	// 방번호와 유저 아이디를 통해 누적시간 저장
	// @PutMapping("/ms/acctime")
	// public AjaxResult updateAcctime(
	// 		@NotNull @Positive Integer sg_id, 
	// 		@NotNull @Pattern(regexp = "^[0-9a-zA-Z가-힣]*$") String id, 
	// 		@NotNull String acctime) {
	@PutMapping("/ms/acctime")
	public AjaxResult updateAcctime(
		@RequestBody Map<String,String> map	
	) {
		Integer sg_id = Integer.parseInt(map.get("sg_id"));
		String id = map.get("id");
		String acctime = map.get("acctime");
		AjaxResult result = new AjaxResult();
		int cnt = 0;
		String status = "fail";
		StringBuilder message = new StringBuilder();

		List<MemberStudyDTO> ms = memberStudyService.getAcctime(sg_id, id);
		if (ms == null || ms.size() == 0) {
			message.append("Acctime data가 없음");
		}

		if (ms.get(0).getAcctime() == null) {
			
			cnt = memberStudyService.updateAcctime(sg_id, id,
					LocalDateTime.parse(acctime, DateTimeFormatter.ISO_DATE_TIME));
		} else {
			LocalDateTime temp = ms.get(0).getAcctime();

			LocalDateTime time = LocalDateTime.parse(acctime, DateTimeFormatter.ISO_DATE_TIME);
//			LocalDateTime baseTime = LocalDateTime.of(1900, 1, 1, 15, 32, 8); 
			LocalDateTime baseTime = LocalDateTime.of(1900, 1, 1, 0, 0, 0);
			System.out.println("time:"+time);
			System.out.println("basetime:"+baseTime);
			System.out.println("acctime:"+acctime);
			// var time = new Date(0, 0, 1, temp[0], temp[1], temp[2]);
			// javascript 에서 위의 기준으로 맞춘 시간이 baseTime 기준 시간임.

//			temp = temp.plusSeconds(time.minusSeconds(baseTime.getSecond()).getSecond());
			temp = temp.plusMinutes(time.minusMinutes(baseTime.getMinute()).getMinute());
			temp = temp.plusHours(time.minusHours(baseTime.getHour()).getHour());	
			cnt = memberStudyService.updateAcctime(sg_id, id, temp);
		}

		if (cnt == 0) {
			message.append("Acctime update 실패");
		} else {
			status = "OK";
		}
		result.setCnt(cnt);
		result.setMessage(message.toString());
		result.setStatus(status);
		return result;
	}

	// 스터디 룸 나가기
	@PostMapping("ms/roomoutOk")
	public AjaxResult roomoutOk(
			@NotNull @Positive Integer sg_id, 
			@NotNull @Pattern(regexp = "^[0-9a-zA-Z가-힣]*$") String id, 
			Model model) {
		AjaxResult result = new AjaxResult();
		int cnt = 0;
		String status = "fail";
		StringBuilder message = new StringBuilder();
		
		cnt = memberStudyService.outStatus(sg_id, id);
		if (cnt == 0) {
			message.append("방 상태 변경 못함");
		} else {
			status = "OK";
		}
		System.out.println("roomout 중 ");
		
		
		result.setCnt(cnt);
		result.setMessage(message.toString());
		result.setStatus(status);
		
		return result;
	}

	// 스터디 룸 들어오기
	@GetMapping("ms/roomenter")
	public AjaxResult roomenter(
			@NotNull @Positive Integer sg_id, 
			@NotNull @Pattern(regexp = "^[0-9a-zA-Z가-힣]*$") String id, 
			Model model) {
		AjaxResult result = new AjaxResult();
		int cnt = 0;
		String status = "fail";
		StringBuilder message = new StringBuilder();

		cnt = memberStudyService.enterStatus(sg_id, id);

		if (cnt == 0) {
			message.append("방 상태 변경 못함");
		} else {
			status = "OK";
		}
		result.setCnt(cnt);
		result.setMessage(message.toString());
		result.setStatus(status);
		return result;
	}

		//스터디 신청
		@GetMapping("/ms/{sg_id}/{id}")
		public AjaxResult RegisterRoom(
				@NotNull @Pattern(regexp = "^[0-9a-zA-Z가-힣]*$") @PathVariable String id,
				@NotNull @Positive @PathVariable Integer sg_id){
			AjaxResult result = new AjaxResult();
			
			
			int cnt = 0; 
			String status = "fail"; 
			StringBuilder message = new StringBuilder(); 
		
			try {
				cnt = memberStudyService.createCommon(id, sg_id);
				if(cnt !=0) {
					status="Ok";
				}
			}catch (Exception e){
				message.append("error:"+e.getMessage());
			}
			result.setCnt(cnt);
			result.setMessage(message.toString());
			result.setStatus(status);
			
			return result;
		}
		
		@PutMapping(value="/ms/{sg_id}/{id}")
		public AjaxResult UpdateCrew(
				@NotNull @Pattern(regexp = "^[0-9a-zA-Z가-힣]*$") @PathVariable String id,
				@NotNull @Positive @PathVariable Integer sg_id){
			AjaxResult result = new AjaxResult();
			
			
			int cnt = 0; 
			String status = "fail"; 
			StringBuilder message = new StringBuilder(); 
		
			try {
				cnt = memberStudyService.updateCrew(id, sg_id);
				if(cnt !=0) {
					status="Ok";
				}
			}catch (Exception e){
				message.append("error:"+e.getMessage());
			}
			result.setCnt(cnt);
			result.setMessage(message.toString());
			result.setStatus(status);
			
			return result;
		}
		
		

}
