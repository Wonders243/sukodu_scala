name := "SudokuScala"

version := "1.0"

scalaVersion := "2.13.12"

libraryDependencies += "org.scalafx" %% "scalafx" % "20.0.0-R31"
assembly / assemblyMergeStrategy := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case "module-info.class" => MergeStrategy.discard
  case x => MergeStrategy.first
}