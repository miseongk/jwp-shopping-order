package cart.service;

import cart.domain.CartItem;
import cart.domain.Member;
import cart.domain.Money;
import cart.domain.Order;
import cart.domain.coupon.Coupon;
import cart.domain.coupon.MemberCoupon;
import cart.dto.OrderDetailResponse;
import cart.dto.OrderRequest;
import cart.dto.OrderResponse;
import cart.exception.CartItemException;
import cart.exception.CouponException;
import cart.exception.ExceptionType;
import cart.exception.OrderException;
import cart.repository.CartItemRepository;
import cart.repository.MemberCouponRepository;
import cart.repository.OrderRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class OrderService {

    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final MemberCouponRepository memberCouponRepository;
    private final CouponService couponService;

    public OrderService(
            CartItemRepository cartItemRepository,
            OrderRepository orderRepository,
            MemberCouponRepository memberCouponRepository,
            CouponService couponService
    ) {
        this.cartItemRepository = cartItemRepository;
        this.orderRepository = orderRepository;
        this.memberCouponRepository = memberCouponRepository;
        this.couponService = couponService;
    }

    public Long register(OrderRequest orderRequest, Member member) {
        List<CartItem> cartItems = orderRequest.getCartItemIds().stream()
                .map(this::getCartItem)
                .collect(Collectors.toList());

        MemberCoupon memberCoupon = getMemberCoupon(orderRequest.getCouponId(), member);

        Order order = Order.of(member, cartItems, orderRequest.getDeliveryFee(), memberCoupon);
        Money requestedOrderPrice = new Money(orderRequest.getTotalOrderPrice());
        order.validateTotalPrice(requestedOrderPrice);

        Order savedOrder = orderRepository.save(order);
        deleteOrdered(cartItems, memberCoupon);

        Money totalOrderPrice = order.calculateTotalPrice();
        if (order.isUnusedCoupon()) {
            couponService.issueByOrderPrice(totalOrderPrice, member);
        }
        return savedOrder.getId();
    }

    private MemberCoupon getMemberCoupon(Long couponId, Member member) {
        if (couponId == -1L) {
            return new MemberCoupon(member, Coupon.NONE);
        }
        return memberCouponRepository.findById(couponId)
                .orElseThrow(() -> new CouponException(ExceptionType.NOT_FOUND_COUPON));
    }

    private void deleteOrdered(List<CartItem> cartItems, MemberCoupon memberCoupon) {
        cartItems.stream()
                .map(CartItem::getId)
                .forEach(cartItemRepository::deleteById);
        if (memberCoupon.isExists()) {
            memberCouponRepository.delete(memberCoupon);
        }
    }

    private CartItem getCartItem(Long cartItemId) {
        return cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new CartItemException(ExceptionType.NOT_FOUND_CART_ITEM));
    }

    @Transactional(readOnly = true)
    public OrderDetailResponse findById(Long id, Member member) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderException(ExceptionType.NOT_FOUND_ORDER));
        order.validateOwner(member);
        return OrderDetailResponse.from(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findAll(Member member) {
        List<Order> orders = orderRepository.findAllByMember(member);

        return orders.stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
    }
}
