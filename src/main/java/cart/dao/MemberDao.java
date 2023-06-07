package cart.dao;

import cart.entity.AuthMemberEntity;
import cart.entity.MemberEntity;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class MemberDao {

    private static final RowMapper<AuthMemberEntity> authMemberRowMapper = (rs, rowNum) -> {
        long id = rs.getLong("id");
        String email = rs.getString("email");
        String password = rs.getString("password");
        return new AuthMemberEntity(new MemberEntity(id, email), password);
    };

    private static final RowMapper<MemberEntity> memberRowMapper = (rs, rowNum) -> {
        long id = rs.getLong("id");
        String email = rs.getString("email");
        return new MemberEntity(id, email);
    };

    private final JdbcTemplate jdbcTemplate;

    public MemberDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long save(AuthMemberEntity member) {
        String sql = "INSERT INTO member (email, password) VALUES (?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    sql, Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, member.getMemberEntity().getEmail());
            ps.setString(2, member.getPassword());
            return ps;
        }, keyHolder);

        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }

    public Optional<AuthMemberEntity> findAuthMemberById(Long id) {
        String sql = "SELECT * FROM member WHERE id = ?";
        try {
            AuthMemberEntity memberEntity = jdbcTemplate.queryForObject(sql, authMemberRowMapper, id);
            return Optional.ofNullable(memberEntity);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<MemberEntity> findById(Long id) {
        String sql = "SELECT id, email FROM member WHERE id = ?";
        try {
            MemberEntity memberEntity = jdbcTemplate.queryForObject(sql, memberRowMapper, id);
            return Optional.ofNullable(memberEntity);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<AuthMemberEntity> findAuthMemberByEmail(String email) {
        String sql = "SELECT * FROM member WHERE email = ?";
        try {
            AuthMemberEntity memberEntity = jdbcTemplate.queryForObject(sql, authMemberRowMapper, email);
            return Optional.ofNullable(memberEntity);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<MemberEntity> findByEmail(String email) {
        String sql = "SELECT id, email FROM member WHERE email = ?";
        try {
            MemberEntity memberEntity = jdbcTemplate.queryForObject(sql, memberRowMapper, email);
            return Optional.ofNullable(memberEntity);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<AuthMemberEntity> findAllAuthMember() {
        String sql = "SELECT * from member";
        return jdbcTemplate.query(sql, authMemberRowMapper);
    }
}

