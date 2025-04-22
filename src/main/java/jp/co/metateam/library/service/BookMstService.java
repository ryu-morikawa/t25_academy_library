package jp.co.metateam.library.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import io.micrometer.common.util.StringUtils;
import jp.co.metateam.library.model.Account;
import jp.co.metateam.library.model.AccountDto;
import jp.co.metateam.library.model.BookMst;
import jp.co.metateam.library.model.BookMstDto;
import jp.co.metateam.library.repository.BookMstRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BookMstService {

    private final BookMstRepository bookMstRepository;
    
    @Autowired
    public BookMstService(BookMstRepository bookMstRepository){
        this.bookMstRepository = bookMstRepository;
    }
    
    public List<BookMstDto> findAvailableWithStockCount() {
        List<BookMst> books = this.bookMstRepository.findLimitedBook();
        List<BookMstDto> bookMstDtoList = new ArrayList<BookMstDto>();

        // 書籍の在庫数を取得
        // FIXME: 現状は書籍ID毎にDBに問い合わせている。一度のSQLで完了させたい。
        for (int i = 0; i < books.size(); i++) {
            BookMst book = books.get(i);
            BookMstDto bookMstDto = new BookMstDto();
            bookMstDto.setId(book.getId());
            bookMstDto.setIsbn(book.getIsbn());
            bookMstDto.setTitle(book.getTitle());
            bookMstDtoList.add(bookMstDto);
        }

        return bookMstDtoList;
    }

    @PostMapping    
    public Boolean checkEntry (BookMstDto bookMstDto,Model model){
        String booktitle = bookMstDto.getTitle();
        String bookisbn = bookMstDto.getIsbn();
        List<String>errTitleList = new ArrayList<>() ;
        List<String>errIsbnList = new ArrayList<>() ;
        
            if (StringUtils.isEmpty(booktitle)) {
                errTitleList.add("書籍名は必須です");
                model.addAttribute("errTitle",errTitleList);
            } else  if (booktitle.length() > 255 ) {
                errIsbnList.add("書籍名は255文字以内で入力してください");
                model.addAttribute("errTitle",errTitleList);
            }
            if (StringUtils.isEmpty(bookisbn)) {
                errIsbnList.add("ISBN必須です");
                model.addAttribute("errIsbn",errIsbnList);
                return !errTitleList.isEmpty() || !errIsbnList.isEmpty();
            }else if (bookisbn.length() != 13 ) {
                errIsbnList.add("ISBNは13文字で入力してください");
                model.addAttribute("errIsbn",errIsbnList);
            }
            if (!bookisbn.matches("^[0-9]+$")) {
                errIsbnList.add("ISBNは半角数字で入力してください");
                model.addAttribute("errIsbn", errIsbnList);
        }
        if (!errTitleList.isEmpty() || !errIsbnList.isEmpty()) {
           return true;
        }    
        return false;
    }
    public Boolean checkIsbnEntry(BookMstDto bookMstDto,Model model) {
        String getIsbn = bookMstDto.getIsbn();
        List<String>errTitleList = new ArrayList<>() ;
        List<String>errIsbnList = new ArrayList<>() ;
        List<BookMst> bookMst  = this.bookMstRepository.selectByIsbn(getIsbn);
        if (bookMst != null) {
            errIsbnList.add("登録されているISBNです");
            model.addAttribute("errIsbn", errIsbnList);
            return true;}
        return false;
    }
        
            
        
        

    @Transactional
    public void save(BookMstDto bookMstDto) {
        try {
            // AccountDtoからAccountへの変換
            BookMst bookMst = new BookMst();

            bookMst.setTitle(bookMstDto.getTitle());
            bookMst.setIsbn(bookMstDto.getIsbn());

            // データベースへの保存
            this.bookMstRepository.save(bookMst);
        } catch (Exception e) {
            throw e;
        }
    }
    
}



