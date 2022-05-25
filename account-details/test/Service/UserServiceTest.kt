import exceptions.NotFoundException
import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import models.EmailBody
import models.User
import repository.UserRepository
import service.EmailService
import service.UserService

class UserServiceTest : StringSpec() {
    private val mockEmailService  = mockk<EmailService>(relaxed = true)
    private val mockUserRepository = mockk<UserRepository>()
    private val userService: UserService = UserService(mockUserRepository, mockEmailService)


    override suspend fun afterEach(testCase: TestCase, result: TestResult) {
        super.afterEach(testCase, result)
        clearAllMocks()
    }

    override fun afterSpec(f: suspend (Spec) -> Unit) {
        super.afterSpec(f)
        clearAllMocks()
    }

    init{
        "should show welcome msg after entering email" {
            userService.sendWelcomeEmail("aishukashid@gmail.com")
            val expectedEmailBody = EmailBody("Welcome", "Welcome to the portal",
                "aishukashid@gmail.com")
            verify(exactly = 1) { mockEmailService.send(expectedEmailBody) }
        }

        "should send account details if user is present" {
            val email = "userExists@gmail.com"
            val phoneNumber = "1234567890"
            every { mockUserRepository.findByEmail(email) } returns User(phoneNumber, email, "")
            val expectedEmailBody = EmailBody("Account Details",
                "Here is your Registered Phone Number: $phoneNumber", email)
            userService.sendRegisteredPhoneNumber(email)
            verify(exactly = 1) { mockEmailService.send(expectedEmailBody) }
        }

        "should send account not found email if user is not present" {
            val email = "userNotExists@gmail.com"
            every { mockUserRepository.findByEmail(email) }.throws(NotFoundException())
            userService.sendRegisteredPhoneNumber(email)
            val expectedEmailBody = EmailBody("Account Not Found", "We do not have a registered account matching your email address", email)
            verify(exactly = 1) { mockEmailService.send(expectedEmailBody) }
        }
    }
}
