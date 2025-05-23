package tave.websocket.chatserver.member.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tave.websocket.chatserver.common.auth.JwtTokenProvider;
import tave.websocket.chatserver.member.domain.Member;
import tave.websocket.chatserver.member.dto.MemberListReqDto;
import tave.websocket.chatserver.member.dto.MemberLoginReqDto;
import tave.websocket.chatserver.member.dto.MemberSaveReqDto;
import tave.websocket.chatserver.member.service.MemberService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/member")
public class MemberController {
    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;

    public MemberController(MemberService memberService, JwtTokenProvider jwtTokenProvider) {
        this.memberService = memberService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/create")
    public ResponseEntity<?> memberCreate(@RequestBody MemberSaveReqDto memberSaveReqDto) {
        Member member=memberService.create(memberSaveReqDto);
        return new ResponseEntity<>(member.getId(), HttpStatus.CREATED);
    }

    @PostMapping("/doLogin")
    public ResponseEntity<?> doLogin(@RequestBody MemberLoginReqDto memberLoginReqDto) {
        //email, password 검증
        Member member=memberService.login(memberLoginReqDto);

        //일치할 경우 access토큰 발행
        String jwtToken=jwtTokenProvider.createToken(member.getEmail(),member.getRole().toString());;
        Map<String,Object> loginInfo=new HashMap<>();
        loginInfo.put("id",member.getId());
        loginInfo.put("token",jwtToken);
        return new ResponseEntity<>(loginInfo, HttpStatus.OK);

    }

    @GetMapping("/list")
    public ResponseEntity<?> getUserList(){
        List<MemberListReqDto> dtos=memberService.findAll();
        return new ResponseEntity<>(dtos, HttpStatus.OK);
    }

}
