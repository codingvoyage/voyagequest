Module VQLaunch
    Sub Main()
        ' Launch VoyageQuest
        ' by Brian Yang
        Shell("javaw.exe -jar -Djava.library.path=""app\lib\lwjgl-2.9\native\windows"" app\VoyageQuest.jar", vbHide)
    End Sub
End Module
