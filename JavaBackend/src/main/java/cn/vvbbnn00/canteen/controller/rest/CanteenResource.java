package cn.vvbbnn00.canteen.controller.rest;

import cn.vvbbnn00.canteen.annotation.CheckRole;
import cn.vvbbnn00.canteen.dto.request.CanteenListRequest;
import cn.vvbbnn00.canteen.dto.response.BasicDataResponse;
import cn.vvbbnn00.canteen.dto.response.BasicListResponse;
import cn.vvbbnn00.canteen.model.Canteen;
import cn.vvbbnn00.canteen.model.Comment;
import cn.vvbbnn00.canteen.service.CanteenAdminService;
import cn.vvbbnn00.canteen.service.CanteenService;
import cn.vvbbnn00.canteen.service.CommentService;
import cn.vvbbnn00.canteen.util.RequestValidatorUtils;
import jakarta.enterprise.context.RequestScoped;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;

@Path("/canteen")
@RequestScoped
public class CanteenResource {
    @Context
    SecurityContext securityContext;
    CanteenService canteenService = new CanteenService();
    CommentService commentService = new CommentService();
    CanteenAdminService canteenAdminService = new CanteenAdminService();

    @POST
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    @CheckRole("user")
    public BasicListResponse restListCanteen(
            CanteenListRequest canteenListRequest
    ) {
        RequestValidatorUtils.doHibernateValidate(canteenListRequest);
        BasicListResponse response = new BasicListResponse();
        response.setList(canteenService.getCanteenList(
                canteenListRequest.getCurrentPage(),
                canteenListRequest.getPageSize(),
                canteenListRequest.getKw(),
                canteenListRequest.getOrderBy(),
                canteenListRequest.getAsc()
        ));
        response.setTotal(canteenService.getCanteenListCount(canteenListRequest.getKw()));
        response.setPageSize(canteenListRequest.getPageSize());
        response.setCurrentPage(canteenListRequest.getCurrentPage());
        return response;
    }

    @GET
    @Path("/{canteenId}")
    @Produces(MediaType.APPLICATION_JSON)
    @CheckRole("user")
    public BasicDataResponse restGetCanteen(
            @PathParam("canteenId") @NotNull @Min(value = 1, message = "无效的Id") Integer canteenId
    ) {
        // 校验请求参数，请仔细阅读该方法的文档
        RequestValidatorUtils.doHibernateParamsValidate(canteenId);

        BasicDataResponse response = new BasicDataResponse();
        Canteen canteen = canteenService.getCanteenById(canteenId);
        if (canteen == null) {
            return new BasicDataResponse(404, "食堂不存在");
        }
        response.setData(canteen);
        return response;
    }

    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @CheckRole("admin")
    public BasicDataResponse restPostCanteen(Canteen canteen) {
        // 校验请求参数
        RequestValidatorUtils.doHibernateValidate(canteen);

        BasicDataResponse response = new BasicDataResponse();
        try {
            response.setData(canteenService.createCanteen(canteen));
        } catch (Exception e) {
            response.setCode(409);
            response.setMessage(e.getMessage());
        }
        return response;
    }


    @PUT
    @Path("/{canteenId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @CheckRole("canteen_admin")
    public BasicDataResponse restPutCanteen(
            @PathParam("canteenId") @NotNull @Min(value = 1, message = "无效的Id") Integer canteenId,
            Canteen canteen
    ) {
        // 校验请求参数
        RequestValidatorUtils.doHibernateParamsValidate(canteenId, canteen);
        RequestValidatorUtils.doHibernateValidate(canteen);
        Integer userId = Integer.parseInt(securityContext.getUserPrincipal().getName());
        BasicDataResponse response = new BasicDataResponse();
        try {
            canteen.setCanteenId(canteenId);
            response.setData(canteenService.updateCanteen(canteen, userId));
        } catch (Exception e) {
            response.setCode(409);
            response.setMessage(e.getMessage());
        }
        return response;
    }

    @DELETE
    @Path("/{canteenId}")
    @Produces(MediaType.APPLICATION_JSON)
    @CheckRole("admin")
    public BasicDataResponse restDeleteCanteen(
            @PathParam("canteenId") @NotNull @Min(value = 1, message = "无效的Id") Integer canteenId
    ) {
        // 校验请求参数，请仔细阅读该方法的文档
        RequestValidatorUtils.doHibernateParamsValidate(canteenId);

        BasicDataResponse response = new BasicDataResponse();
        try {
            canteenService.deleteCanteen(canteenId);
        } catch (Exception e) {
            response.setCode(500);
            response.setMessage(e.getMessage());
        }
        return response;
    }


    @POST
    @Path("/{canteenId}/comment")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @CheckRole("user")
    public BasicDataResponse restPostComment(
            @PathParam("canteenId") @NotNull @Min(value = 1, message = "无效的Id") Integer canteenId,
            Comment comment
    ) {
        // 校验请求参数，请仔细阅读该方法的文档
        RequestValidatorUtils.doHibernateParamsValidate(canteenId, comment);
        RequestValidatorUtils.doHibernateValidate(comment);

        BasicDataResponse response = new BasicDataResponse();
        try {
            Integer userId = Integer.parseInt(securityContext.getUserPrincipal().getName());
            comment.setReferenceId(canteenId);
            comment.setType(Comment.CommentType.canteen);
            comment.setCreatedBy(userId);
            comment.setParentId(null);
            commentService.createComment(comment, userId);
        } catch (Exception e) {
            response.setCode(409);
            response.setMessage(e.getMessage());
        }
        return response;
    }


    @POST
    @Path("/{canteenId}/comment/{commentId}/reply")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @CheckRole("canteen_admin")
    public BasicDataResponse restPostCommentReply(
            @PathParam("canteenId") @NotNull @Min(value = 1, message = "无效的Id") Integer canteenId,
            @PathParam("commentId") @NotNull @Min(value = 1, message = "无效的Id") Integer commentId,
            Comment comment
    ) {
        // 校验请求参数，请仔细阅读该方法的文档
        RequestValidatorUtils.doHibernateParamsValidate(canteenId, commentId, comment);
        RequestValidatorUtils.doHibernateValidate(comment);

        Integer userId = Integer.parseInt(securityContext.getUserPrincipal().getName());
        if (!canteenAdminService.checkHasCanteenAdmin(canteenId, userId)) {
            throw new RuntimeException("您没有权限回复该评论");
        }

        BasicDataResponse response = new BasicDataResponse();
        try {
            comment.setReferenceId(canteenId);
            comment.setType(Comment.CommentType.canteen);
            comment.setCreatedBy(userId);
            comment.setParentId(commentId);
            comment.setScore(null);

            commentService.createComment(comment, userId);
        } catch (Exception e) {
            response.setCode(409);
            response.setMessage(e.getMessage());
        }
        return response;
    }


    @GET
    @Path("/{canteenId}/comment")
    @Produces(MediaType.APPLICATION_JSON)
    @CheckRole("user")
    public BasicListResponse restListComment(
            @PathParam("canteenId") @NotNull @Min(value = 1, message = "无效的Id") Integer canteenId
    ) {
        // 校验请求参数，请仔细阅读该方法的文档
        RequestValidatorUtils.doHibernateParamsValidate(canteenId);

        BasicListResponse response = new BasicListResponse();
        response.setList(commentService.getCommentList(
                null,
                Comment.CommentType.canteen,
                canteenId,
                null,
                1,
                200,
                "createdAt",
                false
        ));
        response.setTotal(commentService.getCommentListCount(
                null,
                Comment.CommentType.canteen,
                canteenId,
                null
        ));
        response.setPageSize(200);
        response.setCurrentPage(1);
        return response;
    }

    @DELETE
    @Path("/{canteenId}/comment/{commentId}")
    @Produces(MediaType.APPLICATION_JSON)
    @CheckRole("user")
    public BasicDataResponse restDeleteComment(
            @PathParam("canteenId") @NotNull @Min(value = 1, message = "无效的Id") Integer canteenId,
            @PathParam("commentId") @NotNull @Min(value = 1, message = "无效的Id") Integer commentId
    ) {
        // 校验请求参数，请仔细阅读该方法的文档
        RequestValidatorUtils.doHibernateParamsValidate(canteenId, commentId);

        Integer userId = Integer.parseInt(securityContext.getUserPrincipal().getName());
        BasicDataResponse response = new BasicDataResponse();
        try {
            commentService.deleteComment(commentId, userId);
        } catch (Exception e) {
            response.setCode(500);
            response.setMessage(e.getMessage());
        }
        return response;
    }
}