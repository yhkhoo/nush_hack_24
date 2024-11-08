
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nush_hack_24.ChatChoose
import com.example.nush_hack_24.EditPage
import com.example.nush_hack_24.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TuteeScreen(
    vm: MainViewModel = viewModel()
) {
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 }) // Remember the pager state

    // Titles for the tabs
    val titles = listOf("About", "Chat")
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        // TabRow for tab navigation
        TabRow(selectedTabIndex = pagerState.currentPage) {
            titles.forEachIndexed { index, title ->
                Tab(
                    text = { Text(title) },
                    selected = pagerState.currentPage == index,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.scrollToPage(index)
                        }
                    }
                )
            }
        }

        // HorizontalPager to display the content based on the selected tab
        HorizontalPager(
            state = pagerState // Explicitly set the number of pages (equal to titles size)
        ) { page ->
            when (page) {
                0 -> AboutScreen(vm) // Show the About screen
                1 -> ChatChoose() // Show the Chat screen
            }
        }
    }
}

@Composable
fun AboutScreen(vm: MainViewModel) {
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
