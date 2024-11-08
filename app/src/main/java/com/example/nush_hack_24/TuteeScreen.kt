import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nush_hack_24.EditPage
import com.example.nush_hack_24.MainViewModel

@Composable
fun TuteeScreen(
    vm: MainViewModel = viewModel()
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(16.dp)
            .verticalScroll(scrollState),

    ) {
        Text("Logged in as: ${vm.userEmail} (TUTEE)", style = MaterialTheme.typography.headlineLarge)

        Spacer(modifier = Modifier.height(16.dp))

        // Display Profile Data
        Text("Name: ${vm.userName}")
        Text("Age: ${vm.userAge}")
        Text("Bio: ${vm.userBio}")
        Text("Subjects: ${vm.selectedSubjects.joinToString(", ")}")

        Spacer(modifier = Modifier.height(24.dp))
        AnimatedVisibility(
            visible = !vm.isEditProfile
        ) {
            Button(
                onClick = { vm.isEditProfile = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Edit Profile")
            }
        }
        AnimatedVisibility(
            visible = vm.isEditProfile
        ) {
            EditPage()
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Logout Button
        Button(
            onClick = { vm.logoutUser { vm.isUserLoggedIn = false } },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Logout")
        }
    }
}
