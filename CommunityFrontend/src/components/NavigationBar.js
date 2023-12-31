"use client"
import {
    Avatar, Badge,
    Dropdown, DropdownItem,
    DropdownMenu,
    DropdownTrigger, Navbar,
    NavbarBrand,
    NavbarContent,
    NavbarItem, Progress, Image
} from "@nextui-org/react";
import Link from "next/link";
import {fetchApiWithAuth} from "@/utils/api";
import {useEffect, useState} from "react";
import store from "@/utils/store";
import {calcLevelAndProgress, calcLevelColor} from "@/utils/level";
import {usePathname} from 'next/navigation'
import {getMe, logout} from "@/utils/auth";
import useSWR from "swr";
import {NotificationIcon} from "@/components/icons/NotificationIcon";
import {MailFilled} from "@ant-design/icons";

export default function NavigationBar() {
    const [user, setUser] = useState();
    const [nextLevelExp, setNextLevelExp] = useState(0);
    const [progress, setProgress] = useState(0);
    const pathname = usePathname();
    const {data: notification} = useSWR(
        "/api/rest/user/notification/unread/count",
        fetchApiWithAuth,
        {
            refreshInterval: 1000 * 5
        }
    );
    const {data: messageList} = useSWR(
        "/api/rest/user/message/target/list",
        (...args) => fetchApiWithAuth(...args).then(r => r.list), {
            refreshInterval: 1000 * 5
        }
    );
    const [unreadMessageCount, setUnreadMessageCount] = useState(0);

    useEffect(() => {
        getMe().then(userData => {
            setUser(userData);
        });
        const unreadMessageCount = store.session.get("unreadMessageCount");
        if (unreadMessageCount) {
            setUnreadMessageCount(unreadMessageCount);
        }
    }, []);

    useEffect(() => {
        if (!user) return;
        if (!messageList) return;
        const userId = user?.userId;
        const key = `canteen|unread_message_${userId}`;
        let lastMessage = localStorage.getItem(key);
        let messageMap = {};
        let cnt = 0
        try {
            lastMessage = JSON.parse(lastMessage);
            lastMessage.forEach(message => {
                messageMap[message?.userId] = message;
            })
        } catch (e) {
        }
        messageList.forEach(message => {
            const uid = message?.userId;
            if (!messageMap[uid]) {
                cnt += message?.totalMessages;
                return
            }
            if (messageMap[uid]?.totalMessages !== message?.totalMessages) {
                cnt += message?.totalMessages - messageMap[uid]?.totalMessages;
            }
        });
        if (cnt < 0) {
            cnt = 0;
            localStorage.setItem(key, JSON.stringify(messageList));
        }
        setUnreadMessageCount(cnt);
        store.session.set("unreadMessageCount", cnt);
    }, [messageList, user])

    useEffect(() => {
        const {nextLevelExp, progress} = calcLevelAndProgress(user?.point);
        setNextLevelExp(nextLevelExp);
        setProgress(progress * 100);
    }, [user]);

    const doLogout = () => {
        logout().then(r => {
        });
    }

    return <Navbar>
        {/* 占位符，不加这些TailwindCSS无法正常编译 */}
        <div className={"text-gray-500 bg-gray-100 bg-gray-500"}></div>
        <div className={"text-green-500 bg-green-100 bg-green-500"}></div>
        <div className={"text-blue-500 bg-blue-100 bg-blue-500"}></div>
        <div className={"text-orange-500 bg-orange-100 bg-orange-500"}></div>
        <div className={"text-red-500 bg-red-100 bg-red-500"}></div>
        <NavbarBrand className={"pointer-events-none select-none"}>
            <Image src={"/logo.png"} alt={"食堂点评交流社区"} width={48} height={48}
                   className={"rounded-xl"} loading={"lazy"}/>
            <p className="pl-2 font-bold text-inherit">食堂点评交流社区</p>
        </NavbarBrand>

        <NavbarContent className="hidden sm:flex gap-4" justify="center">
            <NavbarItem isActive={pathname === "/"}>
                <Link color="foreground" href="/">
                    首页
                </Link>
            </NavbarItem>
            <NavbarItem isActive={pathname.startsWith("/item")}>
                <Link color="foreground" href="/item">
                    发现美食
                </Link>
            </NavbarItem>
            <NavbarItem isActive={pathname.startsWith("/canteen")}>
                <Link color="foreground" href="/canteen">
                    发现食堂
                </Link>
            </NavbarItem>
            <NavbarItem isActive={pathname.startsWith("/complaint")}>
                <Link color="foreground" href="/complaint">
                    投诉中心
                </Link>
            </NavbarItem>
            {(user?.role === 'admin' || user?.role === 'canteen_admin') &&
                <NavbarItem isActive={pathname.startsWith("/management")}>
                    <Link color="foreground" href="/management">
                        后台管理
                    </Link>
                </NavbarItem>
            }
        </NavbarContent>

        <NavbarContent as="div" justify="end">
            <NavbarItem>
                <div className={"flex items-center gap-2"}>
                    <Link href={"/user/notification"}>
                        <Badge color="danger"
                               content={notification?.data > 99 ? '99+' : notification?.data}
                               isInvisible={notification?.data === 0}
                               shape="circle">
                            <NotificationIcon className="fill-current pt-0.5" size={30}/>
                        </Badge>
                    </Link>
                    <Link href={"/user/message"}>
                        <Badge color="danger" content={unreadMessageCount > 99 ? '99+' : unreadMessageCount}
                               isInvisible={unreadMessageCount === 0}
                               shape="circle">
                            <div className={"text-[28px] overflow-hidden"}>
                                <MailFilled className={"rounded-lg overflow-hidden"}/>
                            </div>
                        </Badge>
                    </Link>
                </div>
            </NavbarItem>
            <Dropdown placement="bottom-end">
                <DropdownTrigger>
                    <div className={"w-[40px] h-[40px] rounded-full overflow-hidden bg-gray-200"}>
                        <Image
                            className="transition-transform rounded-full cursor-pointer"
                            alt={user?.username}
                            src={user?.avatar}
                            width={40}
                            height={40}
                            style={{transform: "rotate(0deg)"}}
                            loading={"lazy"}
                        />
                    </div>
                </DropdownTrigger>
                <DropdownMenu
                    aria-label="Profile Actions"
                    variant="flat"
                    disabledKeys={["level"]}
                >
                    <DropdownItem key="profile" className="h-14 gap-2" as={Link} href={
                        `/user/${user?.userId}`
                    }>
                        <p className="font-bold">{user?.username}</p>
                        <p className="font-semibold text-gray-500">{user?.email}</p>
                    </DropdownItem>

                    <DropdownItem
                        key={"level"}
                        isReadOnly
                        className="opacity-100"
                    >
                        <div className={"pt-2 pb-2"}>
                            <div className={"flex justify-between items-center text-gray-500"}>
                                <div
                                    className={`bg-${calcLevelColor(user?.level)}-100 text-${calcLevelColor(user?.level)}-500 font-bold w-fit pl-2.5 pr-2.5 pt-0.5 pb-0.5 rounded`}>
                                    Lv{user?.level}
                                </div>
                                <div>
                                    {user?.point} / {nextLevelExp}
                                </div>
                            </div>
                            <Progress
                                className={"mt-2"}
                                value={progress}
                                classNames={{
                                    indicator: `bg-${calcLevelColor(user?.level)}-500`,
                                }}
                                size={"sm"}
                            />
                        </div>
                    </DropdownItem>

                    {(!user?.isVerified) &&
                        <DropdownItem key="verify" color={"primary"} as={Link} href={"/user/verify"}>
                            <span className={"text-blue-500 font-bold"}>🪪 认证账户，提升等级</span>
                        </DropdownItem>}
                    <DropdownItem key="profile" as={Link} href={"/user/profile"}>用户信息</DropdownItem>
                    <DropdownItem key="change_password" as={Link}
                                  href={"/user/profile/password"}>修改密码</DropdownItem>
                    <DropdownItem key="notification" as={Link} href={"/user/notification"}>通知中心</DropdownItem>
                    <DropdownItem key="message" as={Link} href={"/user/message"}>短消息</DropdownItem>
                    <DropdownItem key="logout" color="danger" className={"text-red-500"} onClick={doLogout}>
                        退出登录
                    </DropdownItem>
                </DropdownMenu>
            </Dropdown>
        </NavbarContent>
    </Navbar>
}
