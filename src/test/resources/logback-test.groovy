appender("STDOUT", ConsoleAppender) {
  encoder(PatternLayoutEncoder) {
    pattern = "%d %p [%c] - <%m>%n"
  }
}
root(INFO, ["STDOUT"])
