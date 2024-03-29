package shootingstar.var.repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import shootingstar.var.dto.res.QUserAuctionParticipateList;
import shootingstar.var.dto.res.UserAuctionParticipateList;
import shootingstar.var.dto.res.UserAuctionSuccessList;
import shootingstar.var.dto.res.QUserAuctionSuccessList;
import shootingstar.var.enums.type.AuctionType;

import java.time.LocalDateTime;
import java.util.List;
import static shootingstar.var.entity.QAuction.auction;
import static shootingstar.var.entity.QUser.user;
import static shootingstar.var.entity.ticket.QTicket.ticket;

public class AuctionRepositoryImpl implements AuctionRepositoryCustom{
    private final JPAQueryFactory queryFactory;
    public AuctionRepositoryImpl(EntityManager em){
        this.queryFactory = new JPAQueryFactory(em);
    }
    @Override
    public List<UserAuctionSuccessList> findSuccessBeforeList(String userUUID, Pageable pageable) {
        return queryFactory
                .select(new QUserAuctionSuccessList(
                        auction.user.name,
                        auction.meetingDate,
                        user.nickname
                ))
                .from(auction)
                .where(currentHighestBidderIdEq(userUUID) , auction.auctionType.eq(AuctionType.SUCCESS))
                .orderBy(auction.meetingDate.desc())
                .fetch();

    }


    //만남 전 만남후 api 두개 만들어야함
    @Override
    public Page<UserAuctionSuccessList> findAllSuccessBeforeByuserUUID(String userUUID, Pageable pageable) {
        List<UserAuctionSuccessList> contnet = queryFactory
                .select(new QUserAuctionSuccessList(
                        auction.user.name,
                        auction.meetingDate,
                        user.nickname
                ))
                .from(auction)
                .where(currentHighestBidderIdEq(userUUID),auction.auctionType.eq(AuctionType.SUCCESS))
                .orderBy(auction.meetingDate.desc())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(auction.count())
                .from(auction)
                .where(currentHighestBidderIdEq(userUUID),auction.auctionType.eq(AuctionType.SUCCESS));

        return PageableExecutionUtils.getPage(contnet, pageable, countQuery::fetchOne);
    }

    @Override
    public List<UserAuctionSuccessList> findSuccessAfterList(String userUUID, Pageable pageable) {
        return queryFactory
                .select(new QUserAuctionSuccessList(
                        auction.user.name,
                        auction.meetingDate,
                        user.nickname
                ))
                .from(auction)
                .where(currentHighestBidderIdEq(userUUID) , auction.auctionType.eq(AuctionType.SUCCESS))
                .orderBy(auction.meetingDate.asc())
                .fetch();
    }

    @Override
    public Page<UserAuctionSuccessList> findAllSuccessAfterByuserUUID(String userUUID, Pageable pageable) {
        List<UserAuctionSuccessList> contnet = queryFactory
                .select(new QUserAuctionSuccessList(
                        auction.user.name,
                        auction.meetingDate,
                        user.nickname
                ))
                .from(auction)
                .where(currentHighestBidderIdEq(userUUID),auction.auctionType.eq(AuctionType.SUCCESS))
                .orderBy(auction.meetingDate.asc())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(auction.count())
                .from(auction)
                .where(currentHighestBidderIdEq(userUUID),auction.auctionType.eq(AuctionType.SUCCESS));

        return PageableExecutionUtils.getPage(contnet, pageable, countQuery::fetchOne);
    }

    @Override
    public List<UserAuctionParticipateList> findParticipateList(String userUUID, Pageable pageable) {
        return queryFactory
                .select(new QUserAuctionParticipateList(
                        auction.user.name,
                        auction.createdTime,
                        auction.bidCount,
                        auction.currentHighestBidAmount
                ))
                .from(auction)
                //bid에서 useruuid찾아서 경매 참여한거 uuid 찾아오기
                .where(auction.auctionType.eq(AuctionType.PROGRESS))
                .fetch();
//        return null;
    }
    @Override
    public Page<UserAuctionParticipateList> findAllParticipateByuserUUID(String userUUID, Pageable pageable) {
        return null;
    }
    private BooleanExpression currentHighestBidderIdEq(String userUUID){
        return userUUID != null ? auction.currentHighestBidderUUID.eq(userUUID) : null;
    }

}
