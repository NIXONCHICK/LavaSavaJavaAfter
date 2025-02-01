### Использование паттерна "Адаптер" (Adapter) в проекте

#### 1. Какой паттерн был использован?
В проекте был применён паттерн "Адаптер" (Adapter), который реализован в виде маппера `UserMapper`.

#### 2. Где используется паттерн?
Паттерн применяется в пакете:  
**`org/api/mappers/`**  
В этом пакете создан класс `UserMapper`, который отвечает за преобразование сущностей (`User`) в DTO (`UserResponse`, `ProfileResponse`).

#### 3. Зачем нужен `UserMapper`?
До использования паттерна `UserService` содержал дублирующуюся логику преобразования данных, что нарушало принципы:
- **SRP (Single Responsibility Principle)** – сервис отвечал и за бизнес-логику, и за преобразование данных.
- **DRY (Don't Repeat Yourself)** – код `UserResponse.builder()` и `ProfileResponse.builder()` повторялся в разных методах.

#### 4. Что изменилось после применения паттерна?
- Вся логика преобразования вынесена из `UserService` в `UserMapper`.  
- Теперь `UserService` занимается только бизнес-логикой, а `UserMapper` – преобразованием моделей.  
- Код стал чище, легче тестировать и поддерживать.  

#### 5. Как выглядит `UserMapper`?
Файл: **`org/api/mappers/UserMapper.java`**
```java
package org.api.mappers;

@Component
public class UserMapper {

  public UserResponse toUserResponse(User user, String token) {
    return UserResponse.builder()
        .username(user.getUsername())
        .email(user.getEmail())
        .token(token)
        .bio(user.getBio())
        .image(user.getImage())
        .build();
  }

  public ProfileResponse toProfileResponse(User user, boolean following) {
    return ProfileResponse.builder()
        .username(user.getUsername())
        .bio(user.getBio())
        .image(user.getImage())
        .following(following)
        .build();
  }

  public ProfileResponse toProfileResponse(User user) {
    return toProfileResponse(user, false);
  }
}
```

#### 6. Как теперь `UserService` использует `UserMapper`?
Файл: **`org/api/services/UserService.java`**
```java
package org.api.services;

@Service
@AllArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final SubscriptionRepository subscriptionRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;
  private final UserMapper userMapper;

  public UserResponse register(RegisterRequest request, HttpServletResponse response) {
    User user = User.builder()
        .username(request.getUsername())
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))
        .build();
    userRepository.save(user);

    String token = generateTokenAndAddToCookies(user, response);
    return userMapper.toUserResponse(user, token);
  }

  public ProfileResponse getUserProfile(String username, HttpServletRequest request) {
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new RuntimeException("User not found"));

    boolean following = false;
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.isAuthenticated()) {
      String token = jwtService.extractTokenFromCookies(request);
      User currentUser = userRepository.findById(jwtService.extractId(token))
          .orElseThrow(() -> new RuntimeException("User not found"));
      Optional<Subscription> subscription = subscriptionRepository.findBySubscriberAndSubscribedTo(currentUser, user);
      following = subscription.isPresent();
    }

    return userMapper.toProfileResponse(user, following);
  }
}
