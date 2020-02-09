package de.clashsoft.gradle.angular

import groovy.transform.Memoized
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.tasks.Delete

class AngularGradlePlugin implements Plugin<Project> {
	@Override
	void apply(Project project) {
		final AngularGradleConfig config = project.extensions.create('angular', AngularGradleConfig)

		// conventions
		config.packageManager.convention(project.provider {
			readNgConfigPackageManager()
		})

		final String buildConfigArg = project.hasProperty('angular-dev') ? '--configuration=gradle' : '--prod'
		config.buildArgs.convention([ 'build', buildConfigArg ])
		config.packageManagerArgs.convention([ 'install' ])

		config.appDir.convention('src/main/angular')
		config.outputDir.convention(config.appDir.map {
			"$it/dist/angular-gradle-demo"
		})

		// tasks
		project.tasks.register('installAngularDependencies', InstallAngularDependenciesTask) {
			it.group = BasePlugin.BUILD_GROUP
			it.workingDir = config.appDir

			// up-to-date checks cause problems because node_modules is so huge :(
			// just hope the package manager figures out when it does not need to do anything
			// it.inputs.file "$appDir/package.json"
			// it.outputs.files "$appDir/package-lock.json", "$appDir/yarn.lock", "$appDir/pnpm-lock.yaml" // lock files
			// it.outputs.dir "$appDir/node_modules"

			it.executable = mkCmd(config.packageManager.get())
			it.args(config.packageManagerArgs)
		}

		project.tasks.register('buildAngular', BuildAngularTask) {
			it.group = BasePlugin.BUILD_GROUP
			it.dependsOn 'installAngularDependencies'

			it.workingDir = config.appDir

			it.executable = mkCmd('ng')
			it.args(config.buildArgs)

			it.inputs.files(project.fileTree(config.appDir).exclude('dist', 'node_modules'))
			it.outputs.dir(config.appDir.map { "$it/dist" })
		}

		project.tasks.register('cleanAngular', Delete) {
			it.delete "$config.appDir/dist"
		}

		// setup
		if (!project.hasProperty('no-angular')) {
			project.sourceSets.main.resources.srcDir(project.files(config.outputDir).builtBy('buildAngular'))
			project.tasks.named('clean') {
				it.dependsOn 'cleanAngular'
			}
		}
	}

	private static String mkCmd(String stringProperty) {
		return isWindows() ? stringProperty + '.cmd' : stringProperty
	}

	private static boolean isWindows() {
		return System.getProperty('os.name').toUpperCase().contains('WINDOWS')
	}

	@Memoized
	private static String readNgConfigPackageManager() {
		def cmd = mkCmd('ng')
		def process = (cmd + ' config cli.packageManager').execute()
		def local = process.text
		process.waitFor()
		if (process.exitValue() == 0) {
			return local.trim()
		}
		return (cmd + ' config -g cli.packageManager').execute().text.trim()
	}
}
