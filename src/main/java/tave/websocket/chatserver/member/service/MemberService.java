package tave.websocket.chatserver.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tave.websocket.chatserver.member.domain.Member;
import tave.websocket.chatserver.member.dto.MemberSaveReqDto;
import tave.websocket.chatserver.member.repository.MemberRepository;

@Service
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public Member create(MemberSaveReqDto memberSaveReqDto){
        //이미 가입되어 있는 이메일 검증
        if(memberRepository.findByEmail(memberSaveReqDto.getEmail()).isPresent()){
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }
        Member newMember= Member.builder()
                .name(memberSaveReqDto.getName())
                .email(memberSaveReqDto.getEmail())
                .password(memberSaveReqDto.getPassword())
                .build();
        Member member=memberRepository.save(newMember);

        return member;
    }
}
