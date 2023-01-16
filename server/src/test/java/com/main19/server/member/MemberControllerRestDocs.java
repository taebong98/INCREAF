package com.main19.server.member;

import com.google.gson.Gson;
import com.main19.server.ServerApplication;
import com.main19.server.auth.jwt.JwtTokenizer;
import com.main19.server.comment.controller.CommentController;
import com.main19.server.comment.mapper.CommentMapper;
import com.main19.server.comment.service.CommentService;
import com.main19.server.member.controller.MemberController;
import com.main19.server.member.dto.MemberDto;
import com.main19.server.member.entity.Member;
import com.main19.server.member.mapper.MemberMapper;
import com.main19.server.member.service.MemberService;
import com.main19.server.redis.RedisDao;
import com.main19.server.resources.S3MockConfig;
import com.main19.server.s3service.S3StorageService;
import com.main19.server.utils.DocumentUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.List;

import static com.main19.server.utils.DocumentUtils.getRequestPreProcessor;
import static com.main19.server.utils.DocumentUtils.getResponsePreProcessor;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = MemberController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@MockBean(JpaMetamodelMappingContext.class)
@AutoConfigureRestDocs
public class MemberControllerRestDocs {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private Gson gson;
    @MockBean
    private MemberService memberService;
    @MockBean
    private MemberMapper mapper;
    @MockBean
    private JwtTokenizer jwtTokenizer;
    @MockBean
    private S3StorageService storageService;


    @Test
    public void postMemberTest() throws Exception {
        // given
        MemberDto.Post post = new MemberDto.Post("taebong98", "aaa@naver.com", "코드스테이츠", "자기소개", "q1w2e3r4!!");
        String content = gson.toJson(post);

        MemberDto.Response response =
                new MemberDto.Response(
                        1L,
                        "taebong98",
                        "aaa@naver.com",
                        "코드스테이츠",
                        null,
                        "자기소개",
                        new ArrayList<>());

        given(mapper.memberPostToMember(Mockito.any(MemberDto.Post.class))).willReturn(new Member());
        given(memberService.createMember(Mockito.any(Member.class))).willReturn(new Member());
        given(mapper.memberToMemberResponse(Mockito.any(Member.class))).willReturn(response);

        // when
        ResultActions actions = mockMvc.perform(
                post("/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(content)
        );

        // then
        actions
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.userName").value(post.getUserName()))
                .andExpect(jsonPath("$.data.email").value(post.getEmail()))
                .andExpect(jsonPath("$.data.location").value(post.getLocation()))
                .andExpect(jsonPath("$.data.profileText").value(post.getProfileText()))

                .andDo(document(
                        "post-member",
                        getRequestPreProcessor(),
                        getResponsePreProcessor(),
                        requestFields(
                                List.of(
                                        fieldWithPath("userName").type(JsonFieldType.STRING).description("닉네임"),
                                        fieldWithPath("email").type(JsonFieldType.STRING).description("이메일"),
                                        fieldWithPath("location").type(JsonFieldType.STRING).description("소속"),
                                        fieldWithPath("profileText").type(JsonFieldType.STRING).description("자기소개"),
                                        fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호")
                                )
                        ),
                        responseFields(
                                List.of(
                                        fieldWithPath("data.memberId").type(JsonFieldType.NUMBER).description("식별자"),
                                        fieldWithPath("data.userName").type(JsonFieldType.STRING).description("닉네임"),
                                        fieldWithPath("data.email").type(JsonFieldType.STRING).description("이메일"),
                                        fieldWithPath("data.location").type(JsonFieldType.STRING).description("소속"),
                                        fieldWithPath("data.profileImage").type(JsonFieldType.NULL).description("프로필사진"),
                                        fieldWithPath("data.profileText").type(JsonFieldType.STRING).description("자기소개"),
                                        fieldWithPath("data.scrapPostingList").type(JsonFieldType.ARRAY).description("스크랩한 포스팅")
                                )
                        )
                ));
    }

    @Test
    public void getMemberTest() throws Exception {
        // given
        long memberId = 1L;
        MemberDto.Response response =
                new MemberDto.Response(
                        1L,
                        "taebong98",
                        "aaa@naver.com",
                        "코드스테이츠",
                        null,
                        "자기소개",
                        new ArrayList<>());

        // when
        given(memberService.findMember(memberId)).willReturn(new Member());
        given(mapper.memberToMemberResponse(Mockito.any(Member.class))).willReturn(response);

        ResultActions actions = mockMvc.perform(
                RestDocumentationRequestBuilders.get("/members/{member-id}", memberId)
                        .accept(MediaType.APPLICATION_JSON)
        );

        // then
        actions // get요청시 나오는 response 가 맞는지 확인
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.memberId").value(response.getMemberId()))
                .andExpect(jsonPath("$.data.userName").value(response.getUserName()))
                .andExpect(jsonPath("$.data.email").value(response.getEmail()))
                .andExpect(jsonPath("$.data.location").value(response.getLocation()))
                .andExpect(jsonPath("$.data.profileImage").value(response.getProfileImage()))
                .andExpect(jsonPath("$.data.profileText").value(response.getProfileText()))
                .andDo(document(
                        "get-member",
                        getRequestPreProcessor(),
                        getResponsePreProcessor(),
                        pathParameters(
                                List.of(
                                        parameterWithName("member-id").description("회원 식별자")
                                )
                        ),
                        responseFields(
                                List.of(
                                        fieldWithPath("data.memberId").type(JsonFieldType.NUMBER).description("식별자"),
                                        fieldWithPath("data.userName").type(JsonFieldType.STRING).description("닉네임"),
                                        fieldWithPath("data.email").type(JsonFieldType.STRING).description("이메일"),
                                        fieldWithPath("data.location").type(JsonFieldType.STRING).description("소속"),
                                        fieldWithPath("data.profileImage").type(JsonFieldType.NULL).description("프로필사진"),
                                        fieldWithPath("data.profileText").type(JsonFieldType.STRING).description("자기소개"),
                                        fieldWithPath("data.scrapPostingList").type(JsonFieldType.ARRAY).description("스크랩한 포스팅")
                                )
                        )
                ));
    }

    @Test
    public void deleteMemberTest() throws Exception {
        // given
        long memberId = 1L;
        doNothing().when(memberService).deleteMember(memberId, "token");

        // when
        ResultActions actions = mockMvc.perform(
                RestDocumentationRequestBuilders.delete("/members/{member-id}", memberId)
                        .header("Authorization", "Bearer AccessToken")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        actions
                .andExpect(status().isNoContent())
                .andDo(document(
                        "delete-member",
                        getRequestPreProcessor(),
                        getResponsePreProcessor(),
                        pathParameters(
                                parameterWithName("member-id").description("회원 식별자")
                        ),
                        requestHeaders(
                                headerWithName("Authorization").description("Bearer AccessToken")
                        )
                ));
    }
}
