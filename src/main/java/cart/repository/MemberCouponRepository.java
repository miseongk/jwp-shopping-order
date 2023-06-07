package cart.repository;

import cart.dao.CouponDao;
import cart.dao.MemberCouponDao;
import cart.dao.MemberDao;
import cart.domain.coupon.Coupon;
import cart.domain.coupon.MemberCoupon;
import cart.domain.Member;
import cart.entity.CouponEntity;
import cart.entity.MemberCouponEntity;
import cart.entity.MemberEntity;
import cart.exception.CouponException;
import cart.exception.ExceptionType;
import cart.exception.MemberException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

@Repository
public class MemberCouponRepository {

    private final CouponDao couponDao;
    private final MemberCouponDao memberCouponDao;
    private final MemberDao memberDao;

    public MemberCouponRepository(CouponDao couponDao, MemberCouponDao memberCouponDao, MemberDao memberDao) {
        this.couponDao = couponDao;
        this.memberCouponDao = memberCouponDao;
        this.memberDao = memberDao;
    }

    public MemberCoupon save(MemberCoupon memberCoupon) {
        MemberCouponEntity memberCouponEntity = toEntity(memberCoupon);
        Long id = memberCouponDao.save(memberCouponEntity);
        return new MemberCoupon(id, memberCoupon.getMember(), memberCoupon.getCoupon(), memberCoupon.getExpiredDate());
    }

    public void saveAll(List<MemberCoupon> memberCoupons) {
        List<MemberCouponEntity> memberCouponEntities = memberCoupons.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());

        memberCouponDao.saveAll(memberCouponEntities);
    }

    public Optional<MemberCoupon> findById(Long id) {
        Optional<MemberCouponEntity> savedMemberCoupon = memberCouponDao.findById(id);
        if (savedMemberCoupon.isEmpty()) {
            return Optional.empty();
        }
        MemberCouponEntity memberCouponEntity = savedMemberCoupon.get();
        MemberCoupon memberCoupon = toDomain(memberCouponEntity);
        return Optional.of(memberCoupon);
    }

    public List<MemberCoupon> findNotExpiredAllByMember(Member member) {
        List<MemberCouponEntity> memberCouponEntities = memberCouponDao.findAllByMemberId(member.getId());
        return memberCouponEntities.stream()
                .map(this::toDomain)
                .filter(memberCoupon -> !memberCoupon.isExpired())
                .collect(Collectors.toList());
    }

    public void delete(MemberCoupon memberCoupon) {
        memberCouponDao.deleteById(memberCoupon.getId());
    }

    private MemberCouponEntity toEntity(MemberCoupon memberCoupon) {
        return new MemberCouponEntity(
                memberCoupon.getMember().getId(),
                memberCoupon.getCoupon().getId(),
                memberCoupon.getExpiredDate()
        );
    }

    private MemberCoupon toDomain(MemberCouponEntity memberCoupon) {
        Coupon coupon = toCoupon(memberCoupon.getCouponId());
        Member member = toMember(memberCoupon.getMemberId());
        return new MemberCoupon(memberCoupon.getId(), member, coupon, memberCoupon.getExpiredDate());
    }

    private Coupon toCoupon(Long couponId) {
        return couponDao.findById(couponId)
                .map(CouponEntity::toDomain)
                .orElseThrow(() -> new CouponException(ExceptionType.NOT_FOUND_COUPON));
    }

    private Member toMember(Long memberId) {
        return memberDao.findById(memberId)
                .map(MemberEntity::toDomain)
                .orElseThrow(() -> new MemberException(ExceptionType.NOT_FOUND_MEMBER));
    }
}
