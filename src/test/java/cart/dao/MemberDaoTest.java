package cart.dao;

import static org.assertj.core.api.Assertions.assertThat;

import cart.entity.AuthMemberEntity;
import cart.entity.MemberEntity;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;

@DisplayNameGeneration(ReplaceUnderscores.class)
@SuppressWarnings("NonAsciiCharacters")
@JdbcTest
class MemberDaoTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private MemberDao memberDao;

    @BeforeEach
    void setUp() {
        memberDao = new MemberDao(jdbcTemplate);
        jdbcTemplate.update("DELETE FROM member");
    }

    @Test
    void 사용자를_저장한다() {
        // given
        AuthMemberEntity memberEntity = new AuthMemberEntity(new MemberEntity("email@email.com"), "password");

        // when
        Long id = memberDao.save(memberEntity);

        // then
        assertThat(id).isPositive();
    }

    @Test
    void 인증용_사용자를_id로_조회한다() {
        // given
        AuthMemberEntity memberEntity = new AuthMemberEntity(new MemberEntity("email@email.com"), "password");
        Long id = memberDao.save(memberEntity);

        // when
        Optional<AuthMemberEntity> savedMember = memberDao.findAuthMemberById(id);

        // then
        assertThat(savedMember).isPresent();
    }

    @Test
    void 인증용_사용자를_email로_조회한다() {
        // given
        AuthMemberEntity memberEntity = new AuthMemberEntity(new MemberEntity( "email@email.com"), "password");
        memberDao.save(memberEntity);

        // when
        Optional<AuthMemberEntity> savedMember = memberDao.findAuthMemberByEmail("email@email.com");

        // then
        assertThat(savedMember).isPresent();
    }

    @Test
    void 사용자를_id로_조회한다() {
        // given
        AuthMemberEntity memberEntity = new AuthMemberEntity(new MemberEntity("email@email.com"), "password");
        Long id = memberDao.save(memberEntity);

        // when
        Optional<MemberEntity> savedMember = memberDao.findById(id);

        // then
        assertThat(savedMember).isPresent();
        assertThat(savedMember.get().getEmail()).isEqualTo("email@email.com");
    }

    @Test
    void 사용자를_email로_조회한다() {
        // given
        AuthMemberEntity memberEntity = new AuthMemberEntity(new MemberEntity("email@email.com"), "password");
        memberDao.save(memberEntity);

        // when
        Optional<MemberEntity> savedMember = memberDao.findByEmail("email@email.com");

        // then
        assertThat(savedMember).isPresent();
    }

    @Test
    void 전체_사용자를_조회한다() {
        // given
        memberDao.save(new AuthMemberEntity(new MemberEntity( "email1@email.com"), "password"));
        memberDao.save(new AuthMemberEntity(new MemberEntity("email2@email.com"), "password"));

        // when
        List<AuthMemberEntity> allMembers = memberDao.findAllAuthMember();

        // then
        assertThat(allMembers).hasSize(2);
    }
}
