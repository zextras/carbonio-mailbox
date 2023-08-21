#!/bin/bash
Describe 'calculator CLI execution behavior'
  It 'should allow calculations be done by CLI'
    When call echo "hello world"
    The status should be success
    The output should equal 'hello world'
  End
End
