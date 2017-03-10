package githubEnt
import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import gheheaders._

object CommitHistorySimulation {


	val rnd = new scala.util.Random
	val formatter = "ddMMyyyyhh:mm:ss"
	val testDuration = Integer.getInteger("testDuration", 1)
	val minWaitMs      = 10000 milliseconds
	val maxWaitMs      = 30000 milliseconds

	val scn = scenario("commit_history")


	.during(testDuration) {

	feed(csv("username.csv").random)

		.pause(minWaitMs, maxWaitMs)
		.exec(http("CH_Launchpage")
			.get("/login")
			.headers(headers_CTM_9)
			.check(regex("""authenticity_token\" type=\"hidden\" value=\"(.+)\" \/><""").find.saveAs("""authenticity_token""")))

		.pause(minWaitMs, maxWaitMs)
		.exec(http("CH_Login")
			.post("/session")
			.headers(headers_4)
			.formParam("commit", "Sign in")
			.formParam("utf8", "✓")
			.formParam("authenticity_token", "${authenticity_token}")
			.formParam("login", "${username}")
			.formParam("password", "passworD1"))


		.pause(minWaitMs, maxWaitMs)
		.exec(http("LaunchPage")
			.get("/")
			.check(regex("""<a href="\S+"\s+aria-label="Homepage" class="site-footer-mark" title="\D+(.+)\.\d+">""").find.saveAs("""Ghe_version""")))


		.pause(minWaitMs, maxWaitMs)
		.exec(http("CH_Dashboard")
			.get("/orgs/first-org/dashboard")
			.check(regex("""mini-repo-list-item css-truncate" href="\/first-org\/(.+)" data-ga-click="Dashboard""").findAll.saveAs("""Repo""")))


		.exec((session: Session) => {
			val folder = session("Repo").as[List[String]]
			session.set("Random_Repo", folder(math.max(rnd.nextInt(folder.length), 0)))
		})

		.pause(minWaitMs, maxWaitMs)

		.doIf("${Ghe_version}", "2.8") {
			exec(http("CH_ViewRepo")
				.get("/first-org/${Random_Repo}")
				.check(regex("""name="authenticity_token" type="hidden" value="(.+)" \/><\/div>\n\s+<button class="dropdown-item dropdown-signout""").find.saveAs("logout_authtoken"))
				.headers(headers_13))
		}

		.doIf("${Ghe_version}", "2.9") {
			exec(http("CH_ViewRepo")
				.get("/first-org/${Random_Repo}")
				.check(regex("""name="authenticity_token" type="hidden" value="(.+)" \/><\/div>\n\s+<button type="submit" class="dropdown-item dropdown-signout""").find.saveAs("logout_authtoken"))
				.headers(headers_13))
		}


		.exec(http("CH_RecentlyTouchedBranches")
			.get("/first-org/${Random_Repo}/show_partial?partial=tree%2Frecently_touched_branches_list")
			.headers(headers_5))


		.exec(http("CH_ContributorsSize")
			.get("/first-org/${Random_Repo}/contributors_size")
			.headers(headers_5))

		.pause(minWaitMs, maxWaitMs)

		.doIf("${Ghe_version}", "2.8") {
			exec(http("CH_ViewCommitHistory")
				.get("/first-org/${Random_Repo}/commits/master?_pjax=%23js-repo-pjax-container")
				.check(regex("""\/avatars\/u\/(\d+)\?s=72"\s+width="36"\s+\/><\/a>""").findAll.saveAs("""user_avatar"""))
				.headers(headers_22))
		}

		.doIf("${Ghe_version}", "2.9") {
			exec(http("CH_ViewCommitHistory")
			.get("/first-org/${Random_Repo}/commits/master?_pjax=%23js-repo-pjax-container")
			.check(regex("""\/avatars\/u\/(\d+)\?s=72"\s+width="36"\s+height="36""").findAll.saveAs("""user_avatar"""))
			.headers(headers_22))
		}

		.repeat(20)
		{
			exec(http("CH_LoadAvatars")
				.get("/avatars/u/${user_avatar.random()}?s=72"))
		}

		.pause(minWaitMs, maxWaitMs)

		.doIf("${Ghe_version}", "2.8") {
		exec(http("CH_ViewCommitHistoryPaginate")
			.get("/first-org/${Random_Repo}/commits/master?page=10&_pjax=%23js-repo-pjax-container")
			.check(regex("""\/${Random_Repo}\/commit\/(.+)\"\s+class=\"sha\s+btn\s+btn-outline""").findAll.saveAs("""commit_sha"""))
			.headers(headers_7))
			}

		.doIf("${Ghe_version}", "2.9") {
			exec(http("CH_ViewCommitHistoryPaginate")
				.get("/first-org/${Random_Repo}/commits/master?page=1")
				.check(regex("""\/${Random_Repo}\/commit\/(.+)\"\s+class=\"sha\s+btn\s+btn-outline""").findAll.saveAs("""commit_sha"""))
				.headers(headers_7))
		}

		.exec((session: Session) => {
				val folder = session("commit_sha").as[List[String]]
				session.set("random_commit_sha", folder(math.max(rnd.nextInt(folder.length), 0)))
		})


		.pause(minWaitMs, maxWaitMs)
		.exec(http("CH_ViewCommitChanges_Split")
			.get("/first-org/${Random_Repo}/commit/${random_commit_sha}?diff=split")
			.headers(headers_13))

		.exec(http("CH_ViewCommitChanges_Unified")
			.get("/first-org/${Random_Repo}/commit/${random_commit_sha}?diff=unified")
			.headers(headers_13))

		.exec(http("CH_BranchCommits")
			.get("/first-org/${Random_Repo}/branch_commits/${random_commit_sha}")
			.headers(headers_5))


		.pause(minWaitMs, maxWaitMs)
		.exec(http("CH_ViewRepoForCommit")
			.get("/first-org/${Random_Repo}/tree/${random_commit_sha}")
			.check(regex("""css-truncate\s+css-truncate-target"><a href="\/first-org\/${Random_Repo}\/tree\/${random_commit_sha}\/(.+)"\s+class=""").findAll.saveAs("""browse_folder1"""))
			.headers(headers_13))

		.exec((session: Session) => {
			val folder = session("browse_folder1").as[List[String]]
			session.set("browse_folder_1", folder(math.max(rnd.nextInt(folder.length), 0)))
		})

		.exec(http("CH_RecentlyTouchedBranches")
			.get("/first-org/${Random_Repo}/show_partial?partial=tree%2Frecently_touched_branches_list")
			.headers(headers_5))
		.exec(http("CH_TreeCommit")
			.get("/first-org/${Random_Repo}/tree-commit/${random_commit_sha}")
			.headers(headers_5))
		.exec(http("CH_FileList")
			.get("/first-org/${Random_Repo}/file-list/${random_commit_sha}")
			.headers(headers_5))


		.pause(minWaitMs, maxWaitMs)
		.exec(http("CH_BrowseRepo_ForCommit")
			.get("/first-org/${Random_Repo}/tree/${random_commit_sha}/${browse_folder_1}?_pjax=%23js-repo-pjax-container")
			.check(regex("""css-truncate\s+css-truncate-target"><a href="\/first-org\/${Random_Repo}\/tree\/${random_commit_sha}\/${browse_folder_1}\/(.+)"\s+class=""").findAll.optional.saveAs("""browse_folder2"""))
			.check(regex("""css-truncate\s+css-truncate-target"><a href="\/first-org\/${Random_Repo}\/blob\/${random_commit_sha}\/${browse_folder_1}\/(.+)"\s+class=""").findAll.optional.saveAs("""browse_file1"""))
			.headers(headers_7))


		.pause(minWaitMs, maxWaitMs)
		.exec(http("CH_TreeCommit")
			.get("/first-org/${Random_Repo}/tree-commit/${random_commit_sha}/${browse_folder_1}")
			.headers(headers_5))
		.exec(http("CH_FileList")
			.get("/first-org/${Random_Repo}/file-list/${random_commit_sha}/${browse_folder_1}")
			.headers(headers_5))


		.doIf(session => session.contains("browse_folder2")) {
			exec((session: Session) => {
				val folder = session("browse_folder2").as[List[String]]
				session.set("browse_folder_2", folder(math.max(rnd.nextInt(folder.length), 0)))
			})
		}

		.doIf(session => session.contains("browse_file1")) {
			exec((session: Session) => {
				val folder = session("browse_file1").as[List[String]]
				session.set("browse_file_1", folder(math.max(rnd.nextInt(folder.length), 0)))
			})
		}


		.doIfOrElse(session => session.contains("browse_folder_2")) {
			pause(minWaitMs, maxWaitMs)
			.exec(http("CH_BrowseRepo_ForCommit")
				.get("/first-org/${Random_Repo}/tree/${random_commit_sha}/${browse_folder_1}/${browse_folder_2}?_pjax=%23js-repo-pjax-container")
				.check(regex("""css-truncate\s+css-truncate-target"><a href="\/first-org\/${Random_Repo}\/blob\/${random_commit_sha}\/${browse_folder_1}\/${browse_folder_2}\/(.+)"\s+class=""").findAll.optional.saveAs("""browse_file2"""))
				.headers(headers_7))

			.exec(http("CH_TreeCommit")
				.get("/first-org/${Random_Repo}/tree-commit/${random_commit_sha}/${browse_folder_1}/${browse_folder_2}")
				.headers(headers_5))
			.exec(http("CH_FileList")
				.get("/first-org/${Random_Repo}/file-list/${random_commit_sha}/${browse_folder_1}/${browse_folder_2}")
				.headers(headers_5))

			}
			{

			pause(minWaitMs, maxWaitMs)
			.exec(http("CH_BrowseFile_ForCommit")
				.get("/first-org/${Random_Repo}/blob/${random_commit_sha}/${browse_folder_1}/${browse_file_1}?_pjax=%23js-repo-pjax-container")
				.headers(headers_7))

			.exec(http("CH_Blame_ForCommitFile")
				.get("/first-org/${Random_Repo}/blame/${random_commit_sha}/${browse_folder_1}/${browse_file_1}")
				.headers(headers_13))

			.exec(http("CH_History_ForCommitFile")
				.get("/first-org/${Random_Repo}/commits/${random_commit_sha}/${browse_folder_1}/${browse_file_1}")
				.headers(headers_13))

			}




		.doIf(session => session.contains("browse_file2")) {
			exec((session: Session) => {
				val folder = session("browse_file2").as[List[String]]
				session.set("browse_file_2", folder(math.max(rnd.nextInt(folder.length), 0)))
			})

			//browse the file inside the folder
			.pause(minWaitMs, maxWaitMs)
			.exec(http("CH_BrowseFile_ForCommit")
				.get("/first-org/${Random_Repo}/blob/${random_commit_sha}/${browse_folder_1}/${browse_folder_2}/${browse_file_2}?_pjax=%23js-repo-pjax-container")
				.headers(headers_7))

			.exec(http("CH_Contributors_ForCommitFile")
				.get("/first-org/${Random_Repo}/contributors/${random_commit_sha}/${browse_folder_1}/${browse_folder_2}/${browse_file_2}")
				.headers(headers_5))

			.pause(minWaitMs, maxWaitMs)
			.exec(http("CH_Blame_ForCommitFile")
				.get("/first-org/${Random_Repo}/blame/${random_commit_sha}/${browse_folder_1}/${browse_folder_2}/${browse_file_2}")
				.headers(headers_13))

			// -----------click history
			.pause(minWaitMs, maxWaitMs)
			.exec(http("CH_History_ForCommitFile")
				.get("/first-org/${Random_Repo}/commits/${random_commit_sha}/${browse_folder_1}/${browse_folder_2}/${browse_file_2}")
				.headers(headers_13))
		}


		// -------view branches
		.pause(minWaitMs, maxWaitMs)
		.exec(http("CH_ViewBranches")
			.get("/first-org/${Random_Repo}/branches?_pjax=%23js-repo-pjax-container")
			.headers(headers_7))



		// ------logout
		.pause(minWaitMs, maxWaitMs)
		.exec(http("CH_Logout")
			.post("/logout")
			.headers(headers_4)
			.formParam("utf8", "✓")
			.formParam("authenticity_token", "${logout_authtoken}"))

		.exec(_.remove("browse_file1"))
		.exec(_.remove("browse_file_1"))
		.exec(_.remove("browse_folder2"))
		.exec(_.remove("browse_folder_2"))
		.exec(_.remove("browse_file2"))
		.exec(_.remove("browse_file_2"))

	}
}
