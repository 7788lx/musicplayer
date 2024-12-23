# musicplayer
it is a very simple music player for Android system developed by AI(coursor),<br>
这是一个用AI(cursor)开发的，非常简单的安卓音乐播放器。<br>
播放器界面：<br>
<img src="https://github.com/user-attachments/assets/7d9f9282-2fd1-4ccd-b692-010826a737ea" width="350">

开发故事：

该APP是一个简单的本地音乐播放器，使用Android Studio和Cursor进行编码、调试、发布。
这是我第一次用AI工具进行开发的安卓APP，之前没有任何写安卓APP的经验，从下载AS和Cursor，一共花了3天时间完成。

软件开发完全是cursor自动写的代码，我就是一个负责调试，并将AS报错复制粘贴到cursor，然后由它自动修改代码的工具人。

我觉得AI已经可以替代程序员啦，以后程序员只需要会复制粘贴，就可以完成软件代码的编写，AI比人类更了解代码的实现。

APP的功能：
只要你将下载的mp3音乐文件，放到安卓系统的根目录的mp3文件夹里，
该播放器就会自动扫描里面的音乐文件，并可以播放音乐。
你也可以点圆圈+号，自定义文件夹；
点击圆圈箭头符号，刷新文件夹里的音乐，用于你下载新歌后，将新音乐更新到播放器；
点击圆点，可以定位到你当前正在播放的音乐；
点击向上的按钮，返回清单第一首歌；
点击三根杠按钮，可以排序音乐。
点击放大镜按钮，可以搜索歌曲。

目前没有实现歌词关联功能，以后有时间再用AI去实现，或者哪位帅哥美女帮忙优化和改进。

下面是我给cursor提出的开发需求。
音乐播放器需求：
1、主界面上半部分80%是一个音乐列表，下面20%是播放按钮，有播放暂停和上下曲3个按钮，在右边增加一个播放模式的按钮，可以顺序播放或者随机播放。<br>
2、app默认自动读取/mp3/里面的音乐文件，并按修改时间倒序排列在主界面的列表里。<br>
3、app运行在安卓14的小米手机上，小米系统是澎湃OS，要求打开app时提供选择访问本地目录的权限。<br>
4、app支持搜索功能，在app最上方最左边是个放大镜按钮，用于搜索，有排序功能，在顶部最右边，排序按钮左边有一个返回顶部的按钮。<br>
5、可以参考github上面的APlayer进行编码，https://github.com/rRemix/APlayer<br>
6、要求音乐播放的时候，在下拉菜单显示音乐播放的控制按钮，在状态栏显示音乐播放器的小logo。<br>
7、要求把代码的注释都写上，便于我阅读和理解。<br>

编码全自动，我没有写一行代码，时代变了，软件开发的效率已经比过去更加高效，

感谢AI，感谢AI研发工程师，把程序员从繁重的编码和调试的工作中解放啦！

下载使用：
我把apk文件上传到code里了，路径如下，大家可以直接下载使用：
/app/build/outputs/apk/debug/app-debug.apk

我在使用github发布apk的时候遇到如下问题：<br>
刚用github的release发布半天发布不了，一直提示<br>
There was an error creating your Release: tag name can't be blank, tag name is not well-formed, published releases must have a valid tag.<br>
气死我了，半天通过不了，我的tag已经打了，它就不让我发布，不知道为什么，希望大神指导。

我的email是：2073874@qq.com<br>
微信号是：xm7788lx<br>
欢迎交流。<br>



