import com.fruitmkt.service.ReviewService;
import com.fruitmkt.model.entity.Review;
import java.util.List;

public class TestReviewService {
    public static void main(String[] args) {
        try {
            ReviewService service = new ReviewService();
            List<Review> reviews = service.getAllReviewsForAdmin();
            System.out.println("Success! Found " + reviews.size() + " reviews.");
            for (Review r : reviews) {
                System.out.println(r.getCustomerName() + " - " + r.getProductName());
            }
        } catch (Exception e) {
            System.err.println("Exception occurred!");
            e.printStackTrace();
        }
    }
}
