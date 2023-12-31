"use client";
import {Accordion, AccordionItem, Avatar, Button, Chip, Textarea} from "@nextui-org/react";
import {useEffect, useRef, useState} from "react";
import {getMe} from "@/utils/auth";
import useSWR from "swr";
import {fetchApiWithAuth} from "@/utils/api";
import Link from "next/link";
import {formatDateTimeFromNow} from "@/utils/string";
import {message, Modal, Rate} from "antd";
import {useFormStatus} from "react-dom";
import {DeleteOutlined, ExclamationCircleFilled, StarFilled} from "@ant-design/icons";
import EmojiButton from "@/components/common/EmojiButton";
import ReplyPanel from "@/app/canteen/[canteenId]/ReplyPanel";

const {confirm} = Modal;

const SubmitButton = () => {
    const {pending} = useFormStatus();
    return <Button
        color={"primary"}
        className={"mt-2 rounded-full"}
        type={"submit"}
        isLoading={pending}
    >
        发布
    </Button>
}


export default function CommentPanel({canteenId, type = "canteen"}) {
    const [user, setUser] = useState();
    const [messageApi, contextHolder] = message.useMessage();
    const [refresh, setRefresh] = useState(0);
    const [score, setScore] = useState(0);
    const {data, isLoading, error} = useSWR(
        `/api/rest/${type}/${canteenId}/comment?${refresh}`,
        (...args) => fetchApiWithAuth(...args).then(r => r.list)
    );
    const textareaRef = useRef();
    const [commentList, setCommentList] = useState([]);

    useEffect(() => {
        if (data?.length > 0) {
            const mainComment = data?.filter(comment => comment?.parentId === null);
            for (let comment of mainComment) {
                comment.children = data?.filter(c => c?.parentId === comment?.commentId);
            }
            setCommentList(mainComment);
        }
    }, [data]);

    useEffect(() => {
        getMe().then(userData => {
            setUser(userData);
        });
    }, []);

    async function doCommentTopic(formData) {
        let content = formData.get("content");
        if (!content) {
            messageApi.open({
                type: 'error',
                content: '评论内容不能为空'
            });
            return;
        }
        if (score <= 0) {
            messageApi.open({
                type: 'error',
                content: '评分需要大于0分'
            });
            return;
        }
        content = content.trim();
        await fetchApiWithAuth(`/api/rest/${type}/${canteenId}/comment`, {
            method: "POST",
            body: JSON.stringify({content, score})
        }).then(r => {
            messageApi.open({
                type: 'success',
                content: '评论成功'
            });
            textareaRef.current.reset();
            setContent("");
            setRefresh(refresh + 1);
        }).catch(e => {
            messageApi.open({
                type: 'error',
                content: e.message || "评论失败"
            });
        });
    }


    const doDeleteComment = (commentId) => {
        fetchApiWithAuth(`/api/rest/${type}/${canteenId}/comment/${commentId}`, {
            method: "DELETE"
        }).then(r => {
            messageApi.open({
                type: 'success',
                content: '删除成功'
            });
            setRefresh(refresh + 1);
        }).catch(e => {
            messageApi.open({
                type: 'error',
                content: e.message || "删除失败"
            });
        });
    }


    const showDeleteConfirm = (commentId) => {
        confirm({
            title: '确认删除',
            icon: <ExclamationCircleFilled/>,
            content: '你确定要删除这条评论吗？',
            okText: '确定',
            okType: 'danger',
            cancelText: '取消',
            autoFocusButton: 'cancel',
            onOk() {
                doDeleteComment(commentId);
            },
        });
    };

    const contentRef = useRef();
    const [content, setContent] = useState("");

    const inputEmoji = (emoji) => {
        if (!emoji) return;
        const content = contentRef.current;
        if (!content) return;
        const start = content.selectionStart;
        const end = content.selectionEnd;
        setContent(content.value.substring(0, start) + emoji + content.value.substring(end));
        content.focus();
        content.selectionStart = start + emoji.length;
        content.selectionEnd = start + emoji.length;
    }


    return <div>
        {contextHolder}
        <div className={"flex pl-5 pr-5"}>
            <div className={"pl-2 pt-5"}>
                <Avatar
                    src={user?.avatar}
                    size={"lg"}
                    className={"pointer-events-none select-none"}
                />
            </div>
            <div className={"flex-grow p-5"}>
                <form action={doCommentTopic} ref={textareaRef}>
                    <div className={"flex items-center gap-2 mb-1.5"}>
                        <Rate allowHalf value={score} onChange={setScore}/>
                        <span className={"text-lg text-gray-500 font-bold"}>{score.toFixed(2)}</span>
                    </div>
                    <Textarea className={"w-full"}
                              placeholder={"发布你的回复"}
                              name={"content"}
                              value={content}
                              onChange={(e) => {
                                  setContent(e.target.value);
                              }}
                              isRequired={true}
                              ref={contentRef}
                    />
                    <div className={"flex items-center justify-between"}>
                        <EmojiButton inputEmoji={inputEmoji}/>
                        <SubmitButton/>
                    </div>
                </form>
            </div>
        </div>
        <div className={"border-b border-gray-200"}/>
        <div className={"p-5 pt-3 flex flex-col gap-3"}>
            {
                commentList?.map(comment => {
                    return <div className={"flex flex-row gap-5 border-b border-gray-200"} key={comment?.commentId}>
                        <div className={"pl-2 pt-2"}>
                            <Link href={"/user/" + comment?.user?.userId}>
                                <Avatar
                                    src={comment?.user?.avatar}
                                    size={"lg"}
                                />
                            </Link>
                        </div>
                        <div className={"flex-grow pb-5"}>
                            <div className={"text-gray-500 text-base font-normal flex mt-1"}>
                                <Link href={"/user/" + comment?.user?.userId} className={"flex items-center"}>
                                    <span className={"text-gray-500 font-bold flex items-center"}>
                                       @{comment?.user?.username}
                                    </span>
                                </Link>
                                <span className={"pl-0.5 pr-0.5 text-lg flex items-center"}>·</span>
                                <div className={"text-orange-500 font-bold flex items-center gap-0.5"}>
                                    <div className={"flex items-center text-lg"}><StarFilled/></div>
                                    <div className={"text-medium"}>{comment?.score?.toFixed(2)}</div>
                                </div>
                                <span className={"pl-0.5 pr-0.5 text-lg flex items-center"}>·</span>
                                <span className={"text-gray-500 font-normal flex items-center"}>
                                    {formatDateTimeFromNow(comment?.createdAt)}
                                </span>
                                {(comment?.user?.userId === user?.userId || user?.role === 'admin') &&
                                    <div
                                        className={"ml-1.5 text-lg flex items-center text-red-600 hover:text-red-500 active:text-red-300 cursor-pointer transition-colors"}
                                        onClick={() => showDeleteConfirm(comment?.commentId)}
                                    >
                                        <DeleteOutlined/>
                                    </div>
                                }
                            </div>
                            <div className={"text-gray-700 text-base line-clamp-5"}>
                                {comment?.flagged ? <Accordion isCompact showDivider={false}>
                                    <AccordionItem title={<div
                                        className={"text-red-500 bg-red-50 text-center rounded-lg pt-1.5 pb-1.5"}>
                                        ⚠ 该评论由于存在不友善内容，已被折叠
                                    </div>} indicator={<></>} textValue={"不友善的言论"}>
                                        <pre className={"font-sans text-wrap break-all"}>
                                            {comment?.content}
                                        </pre>
                                    </AccordionItem>
                                </Accordion> : <pre className={"font-sans text-wrap break-all"}>
                                    {comment?.content}
                                </pre>}
                            </div>
                            <div className={"flex flex-col gap-3 mt-2"}>
                                <ReplyPanel replies={comment?.children}/>
                            </div>
                        </div>
                    </div>
                })
            }
        </div>
    </div>
}
