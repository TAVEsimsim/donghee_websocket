package tave.websocket.chatserver.member.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tave.websocket.chatserver.member.domain.Member;
import tave.websocket.chatserver.member.dto.MemberListReqDto;
import tave.websocket.chatserver.member.dto.MemberLoginReqDto;
import tave.websocket.chatserver.member.dto.MemberSaveReqDto;
import tave.websocket.chatserver.member.repository.MemberRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Member create(MemberSaveReqDto memberSaveReqDto){
        //이미 가입되어 있는 이메일 검증
        if(memberRepository.findByEmail(memberSaveReqDto.getEmail()).isPresent()){
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }
        Member newMember= Member.builder()
                .name(memberSaveReqDto.getName())
                .email(memberSaveReqDto.getEmail())
                .password(passwordEncoder.encode(memberSaveReqDto.getPassword()))
                .build();
        Member member=memberRepository.save(newMember);

        return member;
    }

    public Member login(MemberLoginReqDto memberLoginReqDto){
        Member member = memberRepository.findByEmail(memberLoginReqDto.getEmail()).orElseThrow(()-> new EntityNotFoundException());
        if(!passwordEncoder.matches(memberLoginReqDto.getPassword(), member.getPassword())){
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        return member;
    }

    public List<MemberListReqDto> findAll(){
        List<Member> members = memberRepository.findAll();
        List<MemberListReqDto> memberListReqDtos = new ArrayList<>();
        for (Member m: members){
            MemberListReqDto memberListReqDto= new MemberListReqDto();
            memberListReqDto.setId(m.getId());
            memberListReqDto.setName(m.getName());
            memberListReqDto.setEmail(m.getEmail());
            memberListReqDtos.add(memberListReqDto);
        }
        return memberListReqDtos;
    }
}
