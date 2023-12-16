package cn.vvbbnn00.canteen.controller.restful;

import cn.vvbbnn00.canteen.annotation.CheckRole;
import cn.vvbbnn00.canteen.dto.response.BasicDataResponse;
import cn.vvbbnn00.canteen.model.Cuisine;
import cn.vvbbnn00.canteen.model.User;
import cn.vvbbnn00.canteen.service.CuisineService;
import cn.vvbbnn00.canteen.util.GsonFactory;
import cn.vvbbnn00.canteen.util.RequestValidatorUtils;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;

import java.io.IOException;
import java.util.logging.Logger;

@WebServlet(name = "CuisineResourceServlet", value = "/restful/cuisine/*")
public class CuisineResourceServlet extends HttpServlet {

    @Override
    @CheckRole("canteen_admin")
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        CuisineService cuisineService = new CuisineService();
        String pathInfo = request.getPathInfo();
        User user = (User) request.getSession().getAttribute("user");

        // post操作只能是创建菜品
        if (pathInfo != null && !pathInfo.equals("/")) {
            response.sendError(404);
            return;
        }

        // 参数校验
        String requestBody = RequestValidatorUtils.getFullBody(request);
        Cuisine cuisine;
        try {
            cuisine = GsonFactory.getGson().fromJson(requestBody, Cuisine.class);
        } catch (Exception e) {
            response.sendError(400, "请求体格式错误");
            return;
        }

        // 执行Hiberante校验
        try {
            RequestValidatorUtils.doHibernateValidate(cuisine);
        } catch (Exception e) {
            response.sendError(400, e.getMessage());
            return;
        }

        // 创建菜品
        try {
            BasicDataResponse basicDataResponse = new BasicDataResponse();
            basicDataResponse.setData(cuisineService.createCuisine(cuisine, user));
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().println(GsonFactory.getGson().toJson(basicDataResponse));
        } catch (Exception e) {
            response.sendError(409, e.getMessage());
        }
    }

    @Override
    @CheckRole("canteen_admin")
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        CuisineService cuisineService = new CuisineService();
        String pathInfo = request.getPathInfo();
        User user = (User) request.getSession().getAttribute("user");

        // put操作只能是更新菜品
        if (pathInfo == null || pathInfo.equals("/")) {
            response.sendError(404);
            return;
        }

        // 解析路径参数
        Integer cuisineId = RequestValidatorUtils.parseRestIdFromPathInfo(pathInfo, response);
        if (cuisineId == null) {
            return;
        }

        // 参数校验
        String requestBody = RequestValidatorUtils.getFullBody(request);
        Cuisine cuisine;
        try {
            cuisine = GsonFactory.getGson().fromJson(requestBody, Cuisine.class);
        } catch (Exception e) {
            response.sendError(400, "请求体格式错误");
            return;
        }

        // 执行Hiberante校验
        try {
            RequestValidatorUtils.doHibernateValidate(cuisine);
        } catch (Exception e) {
            response.sendError(400, e.getMessage());
            return;
        }

        // 更新菜品
        try {
            BasicDataResponse basicDataResponse = new BasicDataResponse();
            cuisine.setCuisineId(cuisineId);
            basicDataResponse.setData(cuisineService.updateCuisine(cuisine, user));
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().println(GsonFactory.getGson().toJson(basicDataResponse));
        } catch (Exception e) {
            response.sendError(500, e.getMessage());
        }
    }

    @Override
    @CheckRole("user")
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        CuisineService cuisineService = new CuisineService();
        String pathInfo = request.getPathInfo();

        // 解析路径参数
        Integer cuisineId = RequestValidatorUtils.parseRestIdFromPathInfo(pathInfo, response);
        if (cuisineId == null) {
            return;
        }

        // 获取菜品
        Cuisine cuisine = cuisineService.getCuisineById(cuisineId);
        if (cuisine == null) {
            response.sendError(404, "菜品不存在");
            return;
        }

        BasicDataResponse basicDataResponse = new BasicDataResponse();
        basicDataResponse.setData(cuisine);

        response.setContentType("application/json;charset=utf-8");
        response.getWriter().println(GsonFactory.getGson().toJson(basicDataResponse));
    }

    @Override
    @CheckRole("canteen_admin")
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        CuisineService cuisineService = new CuisineService();
        String pathInfo = request.getPathInfo();

        // 解析路径参数
        Integer cuisineId = RequestValidatorUtils.parseRestIdFromPathInfo(pathInfo, response);
        if (cuisineId == null) {
            return;
        }

        // 删除菜品
        try {
            cuisineService.deleteCuisineById(cuisineId, (User) request.getSession().getAttribute("user"));
            GsonFactory.makeSuccessResponse(response, "删除菜品成功");
        } catch (Exception e) {
            response.sendError(500, e.getMessage());
        }
    }
}