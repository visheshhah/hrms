package com.example.hrms.services.social;

import com.example.hrms.dtos.social.*;
import com.example.hrms.entities.*;
import com.example.hrms.enums.ERole;
import com.example.hrms.exceptions.ResourceNotFoundException;
import com.example.hrms.repositories.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final TagsRepository tagsRepository;
    private final PostTagsRepository postTagsRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;

    public Long createPost(CreatePostDto createPostDto, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Employee creator = user.getEmployee();

        Post post = new Post();
        post.setCreatedBy(creator);
        post.setTitle(createPostDto.getTitle());
        post.setDescription(createPostDto.getDescription());

        Post savedPost = postRepository.save(post);

        for (PostTagDto postTagDto : createPostDto.getTags()) {
            PostTags postTag = new PostTags();
            Tags tag = tagsRepository.findById(postTagDto.getTagId()).orElseThrow(() -> new ResourceNotFoundException("Post tag not found"));

            postTag.setPost(savedPost);
            postTag.setTag(tag);
            postTagsRepository.save(postTag);
        }
        return savedPost.getId();

    }

    public void deletePost(Long userId, Long postId) throws AccessDeniedException {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Employee deletedBy = user.getEmployee();

        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        if(!post.getCreatedBy().getId().equals(deletedBy.getId()) && user.getRoles().stream().anyMatch(role -> role.getName()== ERole.ROLE_HR)) {
            throw new AccessDeniedException("You are not allowed to delete this post");
        }

        post.setDeletedBy(deletedBy);
        post.setDeletedAt(Instant.now());
        post.setIsDeleted(Boolean.TRUE);

        postRepository.save(post);
    }

    public Long getLikeCount(Long postId) {
        postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        return likeRepository.countByPostId(postId);
    }

    public Long getCommentCount(Long postId) {
        postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        return commentRepository.countByPostIdAndIsDeletedFalse(postId);
    }

    public List<CommentResponseDto> getComments(Long postId) {
        postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        List<Comment> comments =
                commentRepository.findByPostIdAndIsDeletedFalseOrderByCreatedAtDesc(postId);

        return comments.stream()
                .map(comment -> {
                    CommentResponseDto dto = new CommentResponseDto();
                    dto.setCommentId(comment.getId());
                    dto.setCommentText(comment.getCommentText());
                    dto.setEmployeeId(comment.getAuthor().getId());
                    dto.setEmployeeName(
                            comment.getAuthor().getFirstName() + " " +
                                    comment.getAuthor().getLastName()
                    );
                    dto.setCreatedAt(comment.getCreatedAt());
                    return dto;
                })
                .toList();
    }

//    public List<PostResponseDto> getPosts() {
//        List<Post> posts = postRepository.findAll();
//        return posts.stream()
//                .map(post ->  {
//                    PostResponseDto postResponseDto = new PostResponseDto();
//                    postResponseDto.setId(post.getId());
//                    postResponseDto.setTitle(post.getTitle());
//                    postResponseDto.setDescription(post.getDescription());
//                    postResponseDto.setEmployeeId(post.getCreatedBy().getId());
//                    postResponseDto.setAuthorName(post.getCreatedBy().getFirstName() + " " + post.getCreatedBy().getLastName());
//                    postResponseDto.setCreatedAt(post.getCreatedAt());
//                    postResponseDto.setIsSystemGenerated(post.getIsSystemGenerated());
//
//                    return postResponseDto;
//                }).toList();
//    }


    public List<PostResponseDto> getPosts(int page, int size, Long userId) {

        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Long currentUserId = user.getEmployee().getId();

        boolean isHr = user.getRoles().stream().anyMatch(role -> role.getName()== ERole.ROLE_HR);


        Page<Post> postPage = postRepository.findByIsDeletedFalse(
                PageRequest.of(page, size, Sort.by("createdAt").descending())
        );

        List<Post> posts = postPage.getContent();

        if (posts.isEmpty()) {
            return List.of();
        }

        List<Long> postIds = posts.stream()
                .map(Post::getId)
                .toList();

        // 🔹 Like counts
        Map<Long, Long> likeCountMap =
                likeRepository.countLikesByPostIds(postIds)
                        .stream()
                        .collect(Collectors.toMap(
                                PostCountDto::getPostId,
                                PostCountDto::getCount
                        ));

        // 🔹 Comment counts
        Map<Long, Long> commentCountMap =
                commentRepository.countCommentsByPostIds(postIds)
                        .stream()
                        .collect(Collectors.toMap(
                                PostCountDto::getPostId,
                                PostCountDto::getCount
                        ));

        // 🔹 Posts liked by current user
        List<Long> likedPostIds =
                likeRepository.findLikedPostIdsByUser(postIds, currentUserId);

        Set<Long> likedPostIdSet = new HashSet<>(likedPostIds);

        return posts.stream()
                .map(post -> {

                    PostResponseDto dto = new PostResponseDto();

                    // Core
                    dto.setId(post.getId());
                    dto.setTitle(post.getTitle());
                    dto.setDescription(post.getDescription());
                    dto.setCreatedAt(post.getCreatedAt());
                    dto.setIsSystemGenerated(post.getIsSystemGenerated());

                    // Author
                    dto.setEmployeeId(post.getCreatedBy().getId());
                    dto.setAuthorName(
                            post.getCreatedBy().getFirstName() + " " +
                                    post.getCreatedBy().getLastName()
                    );

                    // Interaction counts
                    dto.setLikeCount(
                            likeCountMap.getOrDefault(post.getId(), 0L)
                    );

                    dto.setCommentCount(
                            commentCountMap.getOrDefault(post.getId(), 0L)
                    );

                    // 🔥 Liked by current user
                    dto.setIsLikedByCurrentUser(
                            likedPostIdSet.contains(post.getId())
                    );

                    // 🔥 Permissions
                    boolean isOwner = post.getCreatedBy().getId().equals(currentUserId);
                    dto.setCanEdit(isOwner);
                    dto.setCanDelete(isOwner || isHr);

                    // 🔹 Tags
                    List<TagsTypeDto> tagDtos = post.getPostTags()
                            .stream()
                            .map(postTag -> {
                                TagsTypeDto tagDto = new TagsTypeDto();
                                tagDto.setId(postTag.getTag().getId());
                                tagDto.setTagName(postTag.getTag().getTagName());
                                return tagDto;
                            })
                            .toList();

                    dto.setPostTags(tagDtos);

                    return dto;
                })
                .toList();
    }

    public PostResponseDto getPostById(Long postId, Long currentUserId) {

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isHr = currentUser.getRoles().stream().anyMatch(role -> role.getName()== ERole.ROLE_HR);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        if (Boolean.TRUE.equals(post.getIsDeleted())) {
            throw new IllegalStateException("Post is deleted");
        }

        Long likeCount = likeRepository.countByPostId(postId);
        Long commentCount = commentRepository.countByPostIdAndIsDeletedFalse(postId);

        boolean isLiked =
                likeRepository.existsByPostIdAndLikedById(
                        postId,
                        currentUser.getEmployee().getId()
                );

        boolean isOwner =
                post.getCreatedBy().getId().equals(currentUser.getEmployee().getId());

        PostResponseDto dto = new PostResponseDto();

        // Core
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setDescription(post.getDescription());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setIsSystemGenerated(post.getIsSystemGenerated());

        // Author
        dto.setEmployeeId(post.getCreatedBy().getId());
        dto.setAuthorName(
                post.getCreatedBy().getFirstName() + " " +
                        post.getCreatedBy().getLastName()
        );

        // Interaction
        dto.setLikeCount(likeCount);
        dto.setCommentCount(commentCount);
        dto.setIsLikedByCurrentUser(isLiked);

        // Permissions
        dto.setCanEdit(isOwner);
        dto.setCanDelete(isOwner || isHr);

        // Tags
        List<TagsTypeDto> tagDtos = post.getPostTags()
                .stream()
                .map(postTag -> {
                    TagsTypeDto tagDto = new TagsTypeDto();
                    tagDto.setId(postTag.getTag().getId());
                    tagDto.setTagName(postTag.getTag().getTagName());
                    return tagDto;
                })
                .toList();

        dto.setPostTags(tagDtos);

        return dto;
    }

    @Transactional
    public PostResponseDto editPost(Long postId,
                                    EditPostDto dto,
                                    Long currentUserId) throws AccessDeniedException {

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isHR = currentUser.getRoles().stream().anyMatch(role -> role.getName()== ERole.ROLE_HR);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        if (Boolean.TRUE.equals(post.getIsDeleted())) {
            throw new IllegalStateException("Cannot edit a deleted post");
        }

        if (Boolean.TRUE.equals(post.getIsSystemGenerated())) {
            throw new IllegalStateException("System generated posts cannot be edited");
        }

        boolean isOwner = post.getCreatedBy().getId().equals(currentUserId);

        if (!isOwner) {
            throw new AccessDeniedException("You are not allowed to edit this post");
        }

        // 🔹 Update basic fields
        post.setTitle(dto.getTitle().trim());
        post.setDescription(dto.getDescription().trim());

        // 🔹 Update tags (replace all)
        if (dto.getTagIds() != null) {

            postTagsRepository.deleteByPostId(postId);

            Set<Long> uniqueTagIds = new HashSet<>(dto.getTagIds());

            for (Long tagId : uniqueTagIds) {

                Tags tag = tagsRepository.findById(tagId)
                        .orElseThrow(() -> new ResourceNotFoundException("Tag not found"));

                PostTags postTag = new PostTags();
                postTag.setPost(post);
                postTag.setTag(tag);

                postTagsRepository.save(postTag);
            }
        }

        // 🔥 Build updated response

        Long likeCount = likeRepository.countByPostId(postId);
        Long commentCount = commentRepository.countByPostIdAndIsDeletedFalse(postId);

        boolean isLiked =
                likeRepository.existsByPostIdAndLikedById(
                        postId,
                        currentUser.getEmployee().getId()
                );

        PostResponseDto response = new PostResponseDto();

        // Core
        response.setId(post.getId());
        response.setTitle(post.getTitle());
        response.setDescription(post.getDescription());
        response.setCreatedAt(post.getCreatedAt());
        response.setIsSystemGenerated(post.getIsSystemGenerated());

        // Author
        response.setEmployeeId(post.getCreatedBy().getId());
        response.setAuthorName(
                post.getCreatedBy().getFirstName() + " " +
                        post.getCreatedBy().getLastName()
        );

        // Interaction
        response.setLikeCount(likeCount);
        response.setCommentCount(commentCount);
        response.setIsLikedByCurrentUser(isLiked);

        // Permissions
        response.setCanEdit(true);
        response.setCanDelete(isOwner || isHR);

        // Tags
        List<TagsTypeDto> tagDtos = post.getPostTags()
                .stream()
                .map(postTag -> {
                    TagsTypeDto tagDto = new TagsTypeDto();
                    tagDto.setId(postTag.getTag().getId());
                    tagDto.setTagName(postTag.getTag().getTagName());
                    return tagDto;
                })
                .toList();

        response.setPostTags(tagDtos);

        return response;
    }
}
