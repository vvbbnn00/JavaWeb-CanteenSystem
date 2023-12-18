package cn.vvbbnn00.canteen.service;

import cn.vvbbnn00.canteen.dao.TopicLikeDao;
import cn.vvbbnn00.canteen.dao.impl.TopicLikeDaoImpl;
import cn.vvbbnn00.canteen.model.Topic;
import cn.vvbbnn00.canteen.model.TopicLike;

public class TopicLikeService {
    private static final TopicLikeDao topicLikeDao = new TopicLikeDaoImpl();
    private static final TopicService topicService = new TopicService();

    public void addTopicLike(Integer userId, Integer topicId) {
        if (userId == null) {
            throw new RuntimeException("用户ID不能为空");
        }
        if (topicId == null) {
            throw new RuntimeException("主题ID不能为空");
        }
        topicService.getTopicById(topicId, userId);

        if (topicLikeDao.queryTopicLikeById(userId, topicId)) {
            throw new RuntimeException("已经喜欢过该话题");
        }
        TopicLike topicLike = new TopicLike();
        topicLike.setUserId(userId);
        topicLike.setTopicId(topicId);

        if (!topicLikeDao.insert(topicLike)) {
            throw new RuntimeException("添加主题喜欢失败");
        }
    }

    public void deleteTopicLike(Integer userId, Integer topicId) {
        if (userId == null) {
            throw new RuntimeException("用户ID不能为空");
        }
        if (topicId == null) {
            throw new RuntimeException("主题ID不能为空");
        }
        if (!topicLikeDao.queryTopicLikeById(userId, topicId)) {
            throw new RuntimeException("没有喜欢过该话题");
        }
        TopicLike topicLike = new TopicLike();
        topicLike.setUserId(userId);
        topicLike.setTopicId(topicId);

        if (!topicLikeDao.delete(topicLike)) {
            throw new RuntimeException("删除主题喜欢失败");
        }
    }

    public Integer countTopicLike(Integer topicId) {
        if (topicId == null) {
            throw new RuntimeException("主题ID不能为空");
        }
        return topicLikeDao.countTopicLikeById(topicId);
    }
}