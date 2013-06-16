Module VQLaunch
    Sub Main()
        ' Launch VoyageQuest
        ' by Brian Yang
        Shell("java -jar -Djava.library.path=""lib\lwjgl-2.9\native\windows"" VoyageQuest.jar", vbNormalFocus)
    End Sub
End Module
