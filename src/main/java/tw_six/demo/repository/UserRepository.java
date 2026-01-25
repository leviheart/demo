package tw_six.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tw_six.demo.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}