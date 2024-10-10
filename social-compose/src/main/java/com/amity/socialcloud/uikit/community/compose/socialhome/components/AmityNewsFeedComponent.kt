package com.amity.socialcloud.uikit.community.compose.socialhome.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.amity.socialcloud.uikit.common.ui.base.AmityBaseComponent
import com.amity.socialcloud.uikit.common.ui.scope.AmityComposePageScope
import com.amity.socialcloud.uikit.community.compose.AmitySocialBehaviorHelper
import com.amity.socialcloud.uikit.community.compose.paging.feed.global.amityGlobalFeedLLS
import com.amity.socialcloud.uikit.community.compose.post.composer.AmityPostComposerHelper
import com.amity.socialcloud.uikit.community.compose.socialhome.AmitySocialHomePageViewModel
import com.amity.socialcloud.uikit.community.compose.story.target.AmityStoryTabComponent
import com.amity.socialcloud.uikit.community.compose.story.target.AmityStoryTabComponentType

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AmityNewsFeedComponent(
    modifier: Modifier = Modifier,
    pageScope: AmityComposePageScope? = null,
) {
    val context = LocalContext.current

    val behavior = remember {
        AmitySocialBehaviorHelper.globalFeedComponentBehavior
    }

    val viewModel = viewModel<AmitySocialHomePageViewModel>()
    val posts = remember { viewModel.getGlobalFeed() }.collectAsLazyPagingItems()

    val lazyListState = rememberLazyListState()
    val postListState by viewModel.postListState.collectAsState()

    val isRefreshing by viewModel.isGlobalFeedRefreshing.collectAsState()

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            viewModel.setGlobalFeedRefreshing()
            posts.refresh()
            AmityPostComposerHelper.clear()
        }
    )

    AmityBaseComponent(
        pageScope = pageScope,
        componentId = "newsfeed"
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ) {
            LazyColumn(
                state = lazyListState,
                modifier = modifier.fillMaxSize()
            ) {
                AmitySocialHomePageViewModel.PostListState.from(
                    loadState = posts.loadState.refresh,
                    itemCount = posts.itemCount,
                ).let(viewModel::setPostListState)

                item {
                    AmityStoryTabComponent(
                        type = AmityStoryTabComponentType.GlobalFeed
                    )
                }

                amityGlobalFeedLLS(
                    modifier = modifier,
                    pageScope = pageScope,
                    globalPosts = posts,
                    postListState = postListState,
                    onClick = {
                        behavior.goToPostDetailPage(
                            context = context,
                            id = it.getPostId()
                        )
                    },
                    onCreateCommunityClicked = {
                        behavior.goToCreateCommunityPage(context)
                    }
                )
            }

            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        }
    }
}