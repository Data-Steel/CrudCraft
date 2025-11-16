/*
 * Copyright (c) 2025 CrudCraft contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.datasteel.crudcraft.sample;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.*;
import nl.datasteel.crudcraft.sample.blog.*;
import nl.datasteel.crudcraft.sample.blog.content.*;
import nl.datasteel.crudcraft.sample.security.RoleType;
import nl.datasteel.crudcraft.sample.user.User;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Seeds the in-memory database with hundreds of sample blog entries.
 * This demonstrates CrudCraft's capabilities with a realistic data volume.
 */
@Component
public class BlogDataSeeder implements CommandLineRunner {

    private final EntityManager entityManager;
    private final Random random = new Random(42); // Fixed seed for reproducibility

    // Sample data arrays
    private static final String[] FIRST_NAMES = {
        "Alice", "Bob", "Carol", "David", "Emma", "Frank", "Grace", "Henry",
        "Iris", "Jack", "Kate", "Liam", "Mia", "Noah", "Olivia", "Peter",
        "Quinn", "Rachel", "Sam", "Tina", "Uma", "Victor", "Wendy", "Xavier",
        "Yara", "Zack", "Anna", "Ben", "Chloe", "Dan", "Eva", "Felix"
    };

    private static final String[] LAST_NAMES = {
        "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller",
        "Davis", "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez",
        "Wilson", "Anderson", "Thomas", "Taylor", "Moore", "Jackson", "Martin",
        "Lee", "Perez", "Thompson", "White", "Harris", "Sanchez", "Clark"
    };

    private static final String[] CATEGORY_NAMES = {
        "Technology", "Travel", "Food", "Health", "Fitness", "Lifestyle",
        "Business", "Finance", "Education", "Science", "Entertainment",
        "Sports", "Fashion", "Music", "Books", "Photography", "Art", "Gaming"
    };

    private static final String[] TAG_NAMES = {
        "tutorial", "guide", "tips", "howto", "review", "news", "opinion",
        "analysis", "beginner", "advanced", "featured", "trending", "popular",
        "productivity", "innovation", "design", "development", "research",
        "industry", "best-practices", "case-study", "tools", "resources"
    };

    private static final String[] TITLE_TEMPLATES = {
        "Introduction to %s",
        "Getting Started with %s",
        "Advanced %s Techniques",
        "The Ultimate Guide to %s",
        "10 Tips for Better %s",
        "Why %s Matters in 2025",
        "How to Master %s",
        "The Future of %s",
        "Common %s Mistakes to Avoid",
        "Best Practices for %s"
    };

    private static final String[] CONTENT_PHRASES = {
        "This comprehensive guide explores the fundamentals and advanced concepts.",
        "In today's fast-paced world, understanding these principles is crucial.",
        "Let's dive deep into the various aspects and their practical applications.",
        "Many experts agree that this approach yields the best results.",
        "Through careful analysis and real-world examples, we can better understand.",
        "The key to success lies in consistent practice and continuous learning.",
        "Recent studies have shown significant improvements when applying these methods.",
        "By following these guidelines, you can achieve remarkable outcomes."
    };

    public BlogDataSeeder(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // Check if data already exists
        Long existingAuthors = entityManager.createQuery("select count(a) from Author a", Long.class)
                .getSingleResult();
        if (existingAuthors != null && existingAuthors > 0) {
            return;
        }

        System.out.println("Seeding blog database with sample data...");

        // Create users
        createUsers();
        System.out.println("Created 3 demo users");

        // Create categories
        List<Category> categories = createCategories();
        System.out.println("Created " + categories.size() + " categories");

        // Create tags
        List<Tag> tags = createTags();
        System.out.println("Created " + tags.size() + " tags");

        // Create authors
        List<Author> authors = createAuthors(50);
        System.out.println("Created " + authors.size() + " authors");

        // Create posts with stats
        List<Post> posts = createPosts(300, authors, categories, tags);
        System.out.println("Created " + posts.size() + " posts");

        // Create articles (concrete class extending Content)
        int articleCount = createArticles(100, authors);
        System.out.println("Created " + articleCount + " articles");

        // Create tutorials (concrete class extending Content)
        int tutorialCount = createTutorials(50, authors);
        System.out.println("Created " + tutorialCount + " tutorials");

        // Create comments
        int commentCount = createComments(500, posts);
        System.out.println("Created " + commentCount + " comments");

        System.out.println("Database seeding completed!");
    }

    private void createUsers() {
        // Create admin user
        User admin = new User();
        admin.setUsername("admin");
        admin.setPasswordHash("password");
        admin.setRoles(Set.of(RoleType.ADMIN));
        entityManager.persist(admin);

        // Create editor user
        User editor = new User();
        editor.setUsername("editor");
        editor.setPasswordHash("password");
        editor.setRoles(Set.of(RoleType.EDITOR));
        entityManager.persist(editor);

        // Create viewer user
        User viewer = new User();
        viewer.setUsername("viewer");
        viewer.setPasswordHash("password");
        viewer.setRoles(Set.of(RoleType.VIEWER));
        entityManager.persist(viewer);
    }

    private List<Category> createCategories() {
        List<Category> categories = new ArrayList<>();
        for (String name : CATEGORY_NAMES) {
            Category category = new Category();
            category.setName(name);
            category.setDescription("Explore the latest in " + name.toLowerCase() + " and related topics.");
            entityManager.persist(category);
            categories.add(category);
        }
        return categories;
    }

    private List<Tag> createTags() {
        List<Tag> tags = new ArrayList<>();
        for (String name : TAG_NAMES) {
            Tag tag = new Tag();
            tag.setName(name);
            entityManager.persist(tag);
            tags.add(tag);
        }
        return tags;
    }

    private List<Author> createAuthors(int count) {
        List<Author> authors = new ArrayList<>();
        Set<String> usedEmails = new HashSet<>();

        for (int i = 0; i < count; i++) {
            String firstName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
            String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
            String name = firstName + " " + lastName;
            
            // Generate unique email
            String email;
            int attempt = 0;
            do {
                email = firstName.toLowerCase() + "." + lastName.toLowerCase() + 
                       (attempt > 0 ? attempt : "") + "@blogexample.com";
                attempt++;
            } while (usedEmails.contains(email));
            usedEmails.add(email);

            Author author = new Author();
            author.setName(name);
            author.setEmail(email);
            author.setBio(generateBio(name));
            entityManager.persist(author);
            authors.add(author);
        }
        return authors;
    }

    private String generateBio(String name) {
        String[] bioTemplates = {
            "%s is a passionate writer with years of experience in their field.",
            "An expert contributor, %s shares insights and knowledge through engaging content.",
            "%s loves exploring new topics and sharing discoveries with readers.",
            "With a background in multiple disciplines, %s brings a unique perspective."
        };
        return String.format(bioTemplates[random.nextInt(bioTemplates.length)], name);
    }

    private List<Post> createPosts(int count, List<Author> authors, List<Category> categories, List<Tag> tags) {
        List<Post> posts = new ArrayList<>();
        OffsetDateTime now = OffsetDateTime.now();

        for (int i = 0; i < count; i++) {
            Post post = new Post();
            
            // Generate title
            Category category = categories.get(random.nextInt(categories.size()));
            String titleTemplate = TITLE_TEMPLATES[random.nextInt(TITLE_TEMPLATES.length)];
            post.setTitle(String.format(titleTemplate, category.getName()));
            
            // Generate content
            post.setContent(generateContent());
            post.setSummary(generateSummary());
            
            // Assign author and category
            post.setAuthor(authors.get(random.nextInt(authors.size())));
            post.setCategory(category);
            
            // Set status and published date
            PostStatus status = PostStatus.values()[random.nextInt(PostStatus.values().length)];
            post.setStatus(status);
            if (status == PostStatus.PUBLISHED) {
                post.setPublishedAt(now.minusDays(random.nextInt(365)));
            }
            
            // Add random tags (1-5 tags per post)
            int tagCount = 1 + random.nextInt(5);
            Set<Tag> postTags = new HashSet<>();
            for (int j = 0; j < tagCount; j++) {
                postTags.add(tags.get(random.nextInt(tags.size())));
            }
            post.setTags(postTags);
            
            entityManager.persist(post);
            
            // Create stats for the post
            PostStats stats = new PostStats();
            stats.setPost(post);
            stats.setViewCount(random.nextInt(10000));
            stats.setLikeCount(random.nextInt(500));
            stats.setShareCount(random.nextInt(100));
            entityManager.persist(stats);
            post.setStats(stats);
            
            posts.add(post);
        }
        return posts;
    }

    private String generateContent() {
        StringBuilder content = new StringBuilder();
        int paragraphs = 3 + random.nextInt(5);
        
        for (int i = 0; i < paragraphs; i++) {
            int sentences = 3 + random.nextInt(4);
            for (int j = 0; j < sentences; j++) {
                content.append(CONTENT_PHRASES[random.nextInt(CONTENT_PHRASES.length)]);
                content.append(" ");
            }
            content.append("\n\n");
        }
        
        return content.toString().trim();
    }

    private String generateSummary() {
        return CONTENT_PHRASES[random.nextInt(CONTENT_PHRASES.length)];
    }

    private int createComments(int count, List<Post> posts) {
        // Only create comments for published posts
        List<Post> publishedPosts = posts.stream()
                .filter(p -> p.getStatus() == PostStatus.PUBLISHED)
                .toList();
        
        if (publishedPosts.isEmpty()) {
            return 0;
        }

        Instant now = Instant.now();
        
        for (int i = 0; i < count; i++) {
            Comment comment = new Comment();
            
            Post post = publishedPosts.get(random.nextInt(publishedPosts.size()));
            comment.setPost(post);
            
            String firstName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
            String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
            comment.setAuthorName(firstName + " " + lastName);
            comment.setAuthorEmail(firstName.toLowerCase() + "." + lastName.toLowerCase() + 
                                  "@example.com");
            
            comment.setContent(generateCommentContent());
            comment.setCreatedAt(now.minusSeconds(random.nextInt(31556926)));
            comment.setApproved(random.nextDouble() > 0.2); // 80% approved
            
            entityManager.persist(comment);
        }
        
        return count;
    }

    private int createArticles(int count, List<Author> authors) {
        OffsetDateTime now = OffsetDateTime.now();
        
        String[] articleSubtitles = {
            "A deep dive into the topic",
            "Everything you need to know",
            "Practical insights and examples",
            "Expert analysis and recommendations",
            "Real-world applications explained"
        };

        for (int i = 0; i < count; i++) {
            Article article = new Article();
            
            article.setTitle("Article: " + TITLE_TEMPLATES[random.nextInt(TITLE_TEMPLATES.length)]
                    .replace("%s", CATEGORY_NAMES[random.nextInt(CATEGORY_NAMES.length)]));
            article.setBody(generateContent());
            article.setSubtitle(articleSubtitles[random.nextInt(articleSubtitles.length)]);
            article.setAuthor(authors.get(random.nextInt(authors.size())));
            article.setReadingTimeMinutes(5 + random.nextInt(20));
            article.setFeatured(random.nextDouble() > 0.8); // 20% featured
            article.setAllowComments(random.nextDouble() > 0.1); // 90% allow comments
            
            ContentStatus status = ContentStatus.values()[random.nextInt(ContentStatus.values().length)];
            article.setStatus(status);
            if (status == ContentStatus.PUBLISHED) {
                article.setPublishedAt(now.minusDays(random.nextInt(365)));
            }
            
            entityManager.persist(article);
        }
        
        return count;
    }

    private int createTutorials(int count, List<Author> authors) {
        OffsetDateTime now = OffsetDateTime.now();
        
        String[] prerequisites = {
            "Basic understanding of the topic",
            "Familiarity with core concepts",
            "No prior experience required",
            "Intermediate knowledge recommended",
            "Advanced understanding assumed"
        };

        for (int i = 0; i < count; i++) {
            Tutorial tutorial = new Tutorial();
            
            tutorial.setTitle("Tutorial: " + TITLE_TEMPLATES[random.nextInt(TITLE_TEMPLATES.length)]
                    .replace("%s", CATEGORY_NAMES[random.nextInt(CATEGORY_NAMES.length)]));
            tutorial.setBody(generateContent());
            tutorial.setAuthor(authors.get(random.nextInt(authors.size())));
            tutorial.setDifficultyLevel(DifficultyLevel.values()[random.nextInt(DifficultyLevel.values().length)]);
            tutorial.setEstimatedDurationMinutes(30 + random.nextInt(180));
            tutorial.setPrerequisites(prerequisites[random.nextInt(prerequisites.length)]);
            
            if (random.nextDouble() > 0.5) {
                tutorial.setGithubRepoUrl("https://github.com/example/tutorial-" + (i + 1));
            }
            
            ContentStatus status = ContentStatus.values()[random.nextInt(ContentStatus.values().length)];
            tutorial.setStatus(status);
            if (status == ContentStatus.PUBLISHED) {
                tutorial.setPublishedAt(now.minusDays(random.nextInt(365)));
            }
            
            entityManager.persist(tutorial);
        }
        
        return count;
    }

    private String generateCommentContent() {
        String[] commentTemplates = {
            "Great article! Very informative and well-written.",
            "Thanks for sharing this valuable information.",
            "I found this really helpful. Looking forward to more posts like this.",
            "Interesting perspective on the topic.",
            "This is exactly what I was looking for. Thank you!",
            "Well explained and easy to understand.",
            "Could you provide more details on this aspect?",
            "Excellent work! Keep it up.",
            "This helped me solve my problem. Much appreciated!",
            "Very thorough and comprehensive guide."
        };
        return commentTemplates[random.nextInt(commentTemplates.length)];
    }
}
